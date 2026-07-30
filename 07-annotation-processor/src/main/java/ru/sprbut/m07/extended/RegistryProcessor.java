package ru.sprbut.m07.extended;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import ru.sprbut.m07.api.Registered;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * <b>Расширенный пример модуля 07.</b>
 * <p>
 * Процессор, который собирает <b>реестр всех помеченных классов</b> и генерирует
 * его единым файлом. Это compile-time аналог {@code @ComponentScan}: список бинов
 * известен уже на этапе сборки, и в runtime не нужно ни сканировать classpath,
 * ни создавать объекты рефлексией.
 * <p>
 * Здесь собрано всё, чего не было в простом {@link ru.sprbut.m07.BuilderProcessor}:
 * <ul>
 *   <li><b>накопление между раундами</b> — реестр нельзя записать сразу, потому что
 *       в следующем раунде могут появиться новые аннотированные классы. Пишем
 *       единственный файл в последнем раунде ({@code processingOver()});</li>
 *   <li><b>JavaPoet</b> вместо ручной сборки строк — типы, импорты и форматирование
 *       берёт на себя библиотека (слайд 69);</li>
 *   <li><b>опции процессора</b> ({@code -Aregistry.package=...}) — параметризация
 *       генерации из настроек сборки;</li>
 *   <li><b>{@code Supplier}-фабрики вместо {@code Class.forName}</b> — сгенерированный
 *       код создаёт объекты обычным {@code new}, и именно поэтому такой подход
 *       работает в native image (модуль 22).</li>
 * </ul>
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Registered")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({RegistryProcessor.PACKAGE_OPTION, RegistryProcessor.CLASS_OPTION})
public class RegistryProcessor extends AbstractProcessor {

    public static final String PACKAGE_OPTION = "registry.package";
    public static final String CLASS_OPTION = "registry.class";

    private static final String DEFAULT_PACKAGE = "ru.sprbut.generated";
    private static final String DEFAULT_CLASS = "GeneratedRegistry";

    /** Накопленные за все раунды записи: имя → класс. */
    private final Map<String, ClassName> registry = new LinkedHashMap<>();

    private int rounds;
    private boolean written;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        rounds++;
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Registered.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "@Registered применим только к классам");
                continue;
            }
            TypeElement type = (TypeElement) element;
            if (type.getModifiers().contains(Modifier.ABSTRACT)) {
                error(element, "Абстрактный класс нельзя зарегистрировать: его нечем создать");
                continue;
            }
            if (!hasUsableConstructor(type)) {
                error(element, "Нужен публичный конструктор без параметров");
                continue;
            }
            String name = resolveName(type);
            ClassName previous = registry.put(name, ClassName.get(type));
            if (previous != null) {
                error(element, "Имя '" + name + "' уже занято классом " + previous);
            }
        }

        // Реестр пишем ровно один раз — в первом же раунде, где нашлись записи.
        //
        // Соблазн отложить запись до processingOver() (вдруг в поздних раундах
        // появятся ещё классы) заканчивается ошибкой: javac предупреждает
        // «File created in the last round will not be subject to annotation
        // processing», и обычный код, который импортирует сгенерированный класс,
        // не компилируется — типа для него ещё не существует.
        //
        // Цена решения честная: если @Registered появится на классе, который сам
        // сгенерирован другим процессором в позднем раунде, в реестр он не попадёт.
        if (!written && !registry.isEmpty()) {
            writeRegistry();
            written = true;
        }
        return true;
    }

    public int rounds() {
        return rounds;
    }

    private String resolveName(TypeElement type) {
        String explicit = type.getAnnotation(Registered.class).value();
        if (!explicit.isBlank()) {
            return explicit;
        }
        String simpleName = type.getSimpleName().toString();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private boolean hasUsableConstructor(TypeElement type) {
        var constructors = javax.lang.model.util.ElementFilter.constructorsIn(type.getEnclosedElements());
        return constructors.isEmpty() || constructors.stream().anyMatch(c ->
                c.getParameters().isEmpty() && c.getModifiers().contains(Modifier.PUBLIC));
    }

    /**
     * Генерация через JavaPoet. Библиотека сама расставит импорты и отформатирует
     * код — при ручной сборке строк это самая трудоёмкая часть.
     */
    private void writeRegistry() {
        String packageName = option(PACKAGE_OPTION, DEFAULT_PACKAGE);
        String className = option(CLASS_OPTION, DEFAULT_CLASS);

        TypeName supplierOfObject = ParameterizedTypeName.get(
                ClassName.get(Supplier.class), ClassName.get(Object.class));
        TypeName mapType = ParameterizedTypeName.get(
                ClassName.get(Map.class), ClassName.get(String.class), supplierOfObject);

        CodeBlock.Builder initializer = CodeBlock.builder().add("$T.of(", Map.class);
        boolean first = true;
        for (Map.Entry<String, ClassName> entry : registry.entrySet()) {
            if (!first) {
                initializer.add(", ");
            }
            first = false;
            // Ссылка на конструктор, а не Class.forName — работает в native image
            initializer.add("$S, $T::new", entry.getKey(), entry.getValue());
        }
        initializer.add(")");

        FieldSpec beans = FieldSpec.builder(mapType, "FACTORIES",
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer.build())
                .build();

        MethodSpec names = MethodSpec.methodBuilder("names")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(java.util.Set.class, String.class))
                .addStatement("return FACTORIES.keySet()")
                .build();

        MethodSpec create = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .returns(Object.class)
                .addStatement("$T<$T> factory = FACTORIES.get(name)", Supplier.class, Object.class)
                .beginControlFlow("if (factory == null)")
                .addStatement("throw new $T($S + name)", IllegalArgumentException.class,
                        "В реестре нет записи: ")
                .endControlFlow()
                .addStatement("return factory.get()")
                .build();

        MethodSpec size = MethodSpec.methodBuilder("size")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(int.class)
                .addStatement("return FACTORIES.size()")
                .build();

        TypeSpec registryType = TypeSpec.classBuilder(className)
                .addJavadoc("Сгенерирован $L. Правки будут потеряны при следующей сборке.\n",
                        RegistryProcessor.class.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(beans)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(names)
                .addMethod(create)
                .addMethod(size)
                .build();

        try {
            JavaFile.builder(packageName, registryType)
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Не удалось записать реестр: " + e.getMessage());
        }
    }

    private String option(String key, String fallback) {
        String value = processingEnv.getOptions().get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private void error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}

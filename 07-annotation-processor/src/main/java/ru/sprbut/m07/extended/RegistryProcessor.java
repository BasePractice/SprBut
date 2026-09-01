/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
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
 *
 * <p>Процессор, который собирает <b>реестр всех помеченных классов</b> и генерирует
 * его единым файлом. Это compile-time аналог {@code @ComponentScan}: список бинов
 * известен уже на этапе сборки, и в runtime не нужно ни сканировать classpath,
 * ни создавать объекты рефлексией.</p>
 *
 * <p>Здесь собрано всё, чего не было в простом {@link ru.sprbut.m07.BuilderProcessor}:
 * <ul>
 * <li><b>накопление между раундами</b> — реестр нельзя записать сразу, потому что
 * в следующем раунде могут появиться новые аннотированные классы. Пишем
 * единственный файл в последнем раунде ({@code processingOver()});</li>
 * <li><b>JavaPoet</b> вместо ручной сборки строк — типы, импорты и форматирование
 * берёт на себя библиотека (слайд 69);</li>
 * <li><b>опции процессора</b> ({@code -Aregistry.package=...}) — параметризация
 * генерации из настроек сборки;</li>
 * <li><b>{@code Supplier}-фабрики вместо {@code Class.forName}</b> — сгенерированный
 * код создаёт объекты обычным {@code new}, и именно поэтому такой подход
 * работает в native image (модуль 22).</li>
 * </ul></p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Registered")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({RegistryProcessor.PACKAGE_OPTION, RegistryProcessor.CLASS_OPTION})
public class RegistryProcessor extends AbstractProcessor {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RegistryProcessor() {
        // нечего инициализировать
    }

    /**
     * Значение {@code PACKAGE_OPTION}.
     */
    public static final String PACKAGE_OPTION = "registry.package";
    /**
     * Значение {@code CLASS_OPTION}.
     */

    public static final String CLASS_OPTION = "registry.class";

    /**
     * Значение {@code DEFAULT_PACKAGE}.
     */
    private static final String DEFAULT_PACKAGE = "ru.sprbut.generated";
    /**
     * Значение {@code DEFAULT_CLASS}.
     */

    private static final String DEFAULT_CLASS = "GeneratedRegistry";

    /**
     * Накопленные за все раунды записи: имя → класс.
     */
    private final Map<String, ClassName> registry = new LinkedHashMap<>();

    /**
     * Раунды обработки.
     */
    private int rounds;
    /**
     * Значение {@code written}.
     */

    private boolean written;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        this.rounds++;
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Registered.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                this.error(element, "@Registered применим только к классам");
                continue;
            }
            final TypeElement type = (TypeElement) element;
            if (type.getModifiers().contains(Modifier.ABSTRACT)) {
                this.error(element, "Абстрактный класс нельзя зарегистрировать: его нечем создать");
                continue;
            }
            if (!this.hasUsableConstructor(type)) {
                this.error(element, "Нужен публичный конструктор без параметров");
                continue;
            }
            final String name = this.resolveName(type);
            final ClassName previous = this.registry.put(name, ClassName.get(type));
            if (previous != null) {
                this.error(element, "Имя '" + name + "' уже занято классом " + previous);
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
        if (!this.written && !this.registry.isEmpty()) {
            this.writeRegistry();
            this.written = true;
        }
        return true;
    }

    /**
     * Раунды обработки.
     * @return Раунды обработки
     */
    public int rounds() {
        return this.rounds;
    }

    private String resolveName(final TypeElement type) {
        final String explicit = type.getAnnotation(Registered.class).value();
        if (!explicit.isBlank()) {
            return explicit;
        }
        final String simpleName = type.getSimpleName().toString();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private boolean hasUsableConstructor(final TypeElement type) {
        final var constructors = javax.lang.model.util.ElementFilter.constructorsIn(type.getEnclosedElements());
        return constructors.isEmpty() || constructors.stream().anyMatch(c ->
                c.getParameters().isEmpty() && c.getModifiers().contains(Modifier.PUBLIC));
    }

    /**
     * Генерация через JavaPoet. Библиотека сама расставит импорты и отформатирует
     * код — при ручной сборке строк это самая трудоёмкая часть.
     */
    private void writeRegistry() {
        final String packageName = this.option(PACKAGE_OPTION, DEFAULT_PACKAGE);
        final String className = this.option(CLASS_OPTION, DEFAULT_CLASS);
        final TypeName supplierOfObject = ParameterizedTypeName.get(
                ClassName.get(Supplier.class), ClassName.get(Object.class));
        final TypeName mapType = ParameterizedTypeName.get(
                ClassName.get(Map.class), ClassName.get(String.class), supplierOfObject);
        final CodeBlock.Builder initializer = CodeBlock.builder().add("$T.of(", Map.class);
        boolean first = true;
        for (Map.Entry<String, ClassName> entry : this.registry.entrySet()) {
            if (!first) {
                initializer.add(", ");
            }
            first = false;
            // Ссылка на конструктор, а не Class.forName — работает в native image
            initializer.add("$S, $T::new", entry.getKey(), entry.getValue());
        }
        initializer.add(")");
        final FieldSpec beans = FieldSpec.builder(mapType, "FACTORIES",
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer.build())
                .build();
        final MethodSpec names = MethodSpec.methodBuilder("names")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(java.util.Set.class, String.class))
                .addStatement("return FACTORIES.keySet()")
                .build();
        final MethodSpec create = MethodSpec.methodBuilder("create")
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
        final MethodSpec size = MethodSpec.methodBuilder("size")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(int.class)
                .addStatement("return FACTORIES.size()")
                .build();
        final TypeSpec registryType = TypeSpec.classBuilder(className)
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
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Не удалось записать реестр: " + e.getMessage());
        }
    }

    private String option(final String key, final String fallback) {
        final String value = processingEnv.getOptions().get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private void error(final Element element, final String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}

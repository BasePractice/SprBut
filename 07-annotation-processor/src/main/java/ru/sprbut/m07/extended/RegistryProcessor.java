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
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import ru.sprbut.m07.api.Registered;

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
 * работает в native image (модуль 26).</li>
 * </ul></p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Registered")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({RegistryProcessor.PACKAGE_OPTION, RegistryProcessor.CLASS_OPTION})
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
public class RegistryProcessor extends AbstractProcessor {

    /**
     * Опция сборки, задающая пакет сгенерированного реестра.
     */
    public static final String PACKAGE_OPTION = "registry.package";

    /**
     * Опция сборки, задающая имя класса реестра.
     */
    public static final String CLASS_OPTION = "registry.class";

    /**
     * Пакет реестра, если опция не задана.
     * @checkstyle SingleUseConstantCheck (3 lines)
     */
    private static final String DEFAULT_PACKAGE = "ru.sprbut.generated";

    /**
     * Имя класса реестра, если опция не задана.
     * @checkstyle SingleUseConstantCheck (3 lines)
     */
    private static final String DEFAULT_CLASS = "GeneratedRegistry";

    /**
     * Накопленные за все раунды записи: имени отвечает класс.
     */
    private final Map<String, ClassName> registry = new LinkedHashMap<>();

    /**
     * Раунды обработки.
     */
    private int rounds;

    /**
     * Признак того, что реестр уже записан.
     */
    private boolean written;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RegistryProcessor() {
        // нечего инициализировать
    }

    // Реестр пишется ровно один раз — в первом же раунде, где нашлись записи.
    //
    // Соблазн отложить запись до processingOver() (вдруг в поздних раундах
    // появятся ещё классы) заканчивается ошибкой: javac предупреждает
    // «File created in the last round will not be subject to annotation
    // processing», и обычный код, который импортирует сгенерированный класс,
    // не компилируется — типа для него ещё не существует.
    //
    // Цена решения честная: если @Registered появится на классе, который сам
    // сгенерирован другим процессором в позднем раунде, в реестр он не попадёт.
    @Override
    @SuppressWarnings("DoNotClaimAnnotations")
    public final boolean process(
        final Set<? extends TypeElement> annotations, final RoundEnvironment env
    ) {
        this.rounds += 1;
        final boolean claimed;
        if (env.processingOver()) {
            claimed = false;
        } else {
            for (final Element element : env.getElementsAnnotatedWith(Registered.class)) {
                this.register(element);
            }
            if (!this.written && !this.registry.isEmpty()) {
                this.writeRegistry();
                this.written = true;
            }
            claimed = true;
        }
        return claimed;
    }

    /**
     * Раунды обработки.
     * @return Раунды обработки
     */
    public int rounds() {
        return this.rounds;
    }

    // один помеченный класс: он попадает в реестр, только если его вообще
    // можно создать обычным new
    private void register(final Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            final TypeElement type = (TypeElement) element;
            if (type.getModifiers().contains(Modifier.ABSTRACT)) {
                this.error(
                    element, "Абстрактный класс нельзя зарегистрировать: его нечем создать"
                );
            } else if (RegistryProcessor.constructible(type)) {
                this.remember(type);
            } else {
                this.error(element, "Нужен публичный конструктор без параметров");
            }
        } else {
            this.error(element, "@Registered применим только к классам");
        }
    }

    // запись в реестр: занятое имя означает, что два класса спорят за одну запись
    private void remember(final TypeElement type) {
        final String name = RegistryProcessor.name(type);
        final ClassName previous = this.registry.put(name, ClassName.get(type));
        if (previous != null) {
            this.error(
                type, String.format("Имя '%s' уже занято классом %s", name, previous)
            );
        }
    }

    // генерация через JavaPoet: библиотека сама расставит импорты
    // и отформатирует код — при ручной сборке строк это самая трудоёмкая часть
    private void writeRegistry() {
        try {
            JavaFile.builder(
                this.option(RegistryProcessor.PACKAGE_OPTION, RegistryProcessor.DEFAULT_PACKAGE),
                this.generated()
            ).skipJavaLangImports(true)
                .indent("    ")
                .build()
                .writeTo(this.processingEnv.getFiler());
        } catch (final IOException failure) {
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format("Не удалось записать реестр: %s", failure.getMessage())
            );
        }
    }

    // описание самого класса реестра
    private TypeSpec generated() {
        return TypeSpec.classBuilder(
            this.option(RegistryProcessor.CLASS_OPTION, RegistryProcessor.DEFAULT_CLASS)
        ).addJavadoc(
            "Сгенерирован $L. Правки будут потеряны при следующей сборке.$L",
            RegistryProcessor.class.getSimpleName(), System.lineSeparator()
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(this.factories())
            .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
            .addMethod(RegistryProcessor.names())
            .addMethod(RegistryProcessor.create())
            .addMethod(RegistryProcessor.size())
            .build();
    }

    // поле-карта: ссылки на конструкторы вместо Class.forName — именно поэтому
    // сгенерированный код работает в native image
    private FieldSpec factories() {
        final CodeBlock.Builder initializer = CodeBlock.builder().add("$T.of(", Map.class);
        boolean first = true;
        for (final Map.Entry<String, ClassName> entry : this.registry.entrySet()) {
            if (!first) {
                initializer.add(", ");
            }
            first = false;
            initializer.add("$S, $T::new", entry.getKey(), entry.getValue());
        }
        initializer.add(")");
        return FieldSpec.builder(
            ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                    ClassName.get(Supplier.class), ClassName.get(Object.class)
                )
            ),
            "FACTORIES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
        ).initializer(initializer.build()).build();
    }

    private static MethodSpec names() {
        return MethodSpec.methodBuilder("names")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ParameterizedTypeName.get(Set.class, String.class))
            .addStatement("return FACTORIES.keySet()")
            .build();
    }

    private static MethodSpec create() {
        return MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(String.class, "name")
            .returns(Object.class)
            .addStatement("$T<$T> factory = FACTORIES.get(name)", Supplier.class, Object.class)
            .beginControlFlow("if (factory == null)")
            .addStatement(
                "throw new $T($S + name)",
                IllegalArgumentException.class,
                "В реестре нет записи: "
            )
            .endControlFlow()
            .addStatement("return factory.get()")
            .build();
    }

    private static MethodSpec size() {
        return MethodSpec.methodBuilder("size")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(int.class)
            .addStatement("return FACTORIES.size()")
            .build();
    }

    // имя из аннотации, а если его нет — имя класса с маленькой буквы
    private static String name(final TypeElement type) {
        final String explicit = type.getAnnotation(Registered.class).value();
        final String name;
        if (explicit.isBlank()) {
            final String simple = type.getSimpleName().toString();
            name = String.format(
                "%s%s", Character.toLowerCase(simple.charAt(0)), simple.substring(1)
            );
        } else {
            name = explicit;
        }
        return name;
    }

    // конструктора нет вовсе — значит есть неявный, публичный у публичного класса
    private static boolean constructible(final TypeElement type) {
        final List<ExecutableElement> constructors =
            ElementFilter.constructorsIn(type.getEnclosedElements());
        return constructors.isEmpty()
            || constructors.stream()
                .anyMatch(
                    candidate -> candidate.getParameters().isEmpty()
                        && candidate.getModifiers().contains(Modifier.PUBLIC)
                );
    }

    private String option(final String key, final String fallback) {
        final String value = this.processingEnv.getOptions().get(key);
        final String option;
        if (value == null || value.isBlank()) {
            option = fallback;
        } else {
            option = value;
        }
        return option;
    }

    private void error(final Element element, final String message) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}

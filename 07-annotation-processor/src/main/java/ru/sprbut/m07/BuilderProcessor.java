/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// генератор исходного кода: строки будущего файла собираются конкатенацией —
// в таком виде видно, какой текст окажется в сгенерированном классе
// @checkstyle StringLiteralsConcatenationCheck disable
package ru.sprbut.m07;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import ru.sprbut.m07.api.GenerateBuilder;

/**
 * Слайды 57–64: {@code AbstractProcessor} — генерация исходного кода на этапе компиляции.
 *
 * <p>Процессор делает две вещи из трёх, перечисленных на слайде:
 * <ul>
 * <li><b>анализирует</b> исходный код — проверяет, что класс подчиняется
 * соглашению JavaBeans, и пишет ошибки через {@link Messager};</li>
 * <li><b>генерирует</b> новый исходник через {@link Filer}.</li>
 * </ul>
 * Третьего — <i>изменения</i> существующего кода — здесь нет намеренно:
 * штатное API этого не умеет, Lombok добивается его хаком AST (слайд 59).</p>
 *
 * <p>Ключевое отличие от рефлексии: работа идёт не с {@code Class}, а с
 * {@link javax.lang.model.element.Element} — моделью <i>исходного текста</i>.
 * Классов ещё не существует, загружать нечего.</p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.GenerateBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class BuilderProcessor extends AbstractProcessor {

    /**
     * Filer для записи файлов.
     */
    private Filer filer;

    /**
     * Значение {@code messager}.
     */
    private Messager messager;

    /**
     * Элементы.
     */
    private Elements elements;

    /**
     * Счётчик раундов — чтобы было видно, что их несколько (модуль 08).
     */
    private int round;

    /**
     * Признак того, что разбираемый класс пригоден для генерации.
     */
    private boolean valid;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public BuilderProcessor() {
        // нечего инициализировать
    }

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public final synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        this.filer = env.getFiler();
        this.messager = env.getMessager();
        this.elements = env.getElementUtils();
    }

    // в последнем, «пустом» раунде генерировать уже нельзя, поэтому обработка
    // идёт только до него; true в ответе означает «аннотации обработаны мной,
    // другим процессорам не передавать»
    @Override
    @SuppressWarnings("DoNotClaimAnnotations")
    public final boolean process(
        final Set<? extends TypeElement> annotations, final RoundEnvironment env
    ) {
        this.round += 1;
        final boolean claimed;
        if (env.processingOver()) {
            claimed = false;
        } else {
            for (final Element element : env.getElementsAnnotatedWith(GenerateBuilder.class)) {
                this.handle(element);
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
        return this.round;
    }

    // --- Анализ --------------------------------------------------------------

    // один помеченный элемент: если это не класс или анализ нашёл ошибки,
    // генерировать нечего — сообщения уже отправлены в Messager
    private void handle(final Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            final TypeElement type = (TypeElement) element;
            final List<Property> properties = this.analyze(type);
            if (!properties.isEmpty() || this.valid) {
                this.generate(type, properties);
            }
        } else {
            this.error(element, "@GenerateBuilder применим только к классам");
        }
    }

    // проверка соглашения JavaBeans и сбор свойств: непригодность кода
    // отмечается флагом, а сами ошибки уходят в Messager
    private List<Property> analyze(final TypeElement type) {
        this.valid = true;
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            this.error(type, "@GenerateBuilder не применим к абстрактному классу");
            this.valid = false;
        }
        if (!BuilderProcessor.constructible(type)) {
            this.error(
                type,
                "Классу нужен публичный конструктор без параметров, билдер создаёт объект именно им"
            );
            this.valid = false;
        }
        final List<Property> properties = new ArrayList<>(0);
        for (final VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (!field.getModifiers().contains(Modifier.STATIC)) {
                this.collect(type, field, properties);
            }
        }
        if (properties.isEmpty() && this.valid) {
            this.warning(type, "У класса нет свойств — сгенерированный билдер будет пустым");
        }
        return properties;
    }

    // одно поле: свойство попадает в билдер только если у него есть сеттер
    private void collect(
        final TypeElement type, final VariableElement field, final List<Property> sink
    ) {
        final String name = field.getSimpleName().toString();
        final String setter = String.format("set%s", BuilderProcessor.capitalize(name));
        if (BuilderProcessor.settable(type, setter)) {
            sink.add(new Property(name, field.asType().toString(), setter));
        } else {
            this.error(
                field,
                String.format("Нет сеттера %s(...), билдер не сможет задать это поле", setter)
            );
            this.valid = false;
        }
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

    private static boolean settable(final TypeElement type, final String setter) {
        return ElementFilter.methodsIn(type.getEnclosedElements())
            .stream()
            .anyMatch(
                method -> method.getSimpleName().contentEquals(setter)
                    && method.getParameters().size() == 1
                    && method.getModifiers().contains(Modifier.PUBLIC)
            );
    }

    // --- Генерация -----------------------------------------------------------

    // файл создаётся через Filer, а не через new FileWriter: иначе javac
    // не узнает о новом коде и не скомпилирует его в следующем раунде
    private void generate(final TypeElement type, final List<Property> properties) {
        final String pack = this.elements.getPackageOf(type).getQualifiedName().toString();
        final String simple = type.getSimpleName().toString();
        final String suffix = type.getAnnotation(GenerateBuilder.class).suffix();
        final String builder = simple + suffix;
        final String qualified;
        if (pack.isEmpty()) {
            qualified = builder;
        } else {
            qualified = String.format("%s.%s", pack, builder);
        }
        try {
            try (
                PrintWriter out = new PrintWriter(
                    this.filer.createSourceFile(qualified, type).openWriter()
                )
            ) {
                if (!pack.isEmpty()) {
                    out.println("package " + pack + ";");
                    out.println();
                }
                out.println(
                    "/** Сгенерирован "
                        + BuilderProcessor.class.getSimpleName()
                        + ". Правки будут потеряны при следующей сборке. */"
                );
                out.println("public final class " + builder + " {");
                out.println();
                for (final Property property : properties) {
                    out.println(
                        "    private " + property.type() + " " + property.name() + ";"
                    );
                }
                out.println();
                out.println("    private " + builder + "() {");
                out.println("    }");
                out.println();
                out.println("    public static " + builder + " create() {");
                out.println("        return new " + builder + "();");
                out.println("    }");
                for (final Property property : properties) {
                    out.println();
                    out.println(
                        "    public "
                            + builder
                            + " "
                            + property.name()
                            + "("
                            + property.type()
                            + " value) {"
                    );
                    out.println("        this." + property.name() + " = value;");
                    out.println("        return this;");
                    out.println("    }");
                }
                out.println();
                out.println("    public " + simple + " build() {");
                out.println("        " + simple + " result = new " + simple + "();");
                for (final Property property : properties) {
                    out.println(
                        "        result." + property.setter() + "(this." + property.name() + ");"
                    );
                }
                out.println("        return result;");
                out.println("    }");
                out.println("}");
            }
        } catch (final IOException failure) {
            this.error(
                type,
                String.format("Не удалось записать %s: %s", qualified, failure.getMessage())
            );
        }
    }

    // --- Диагностика ---------------------------------------------------------

    private void error(final Element element, final String message) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void warning(final Element element, final String message) {
        this.messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    private static String capitalize(final String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Свойство, для которого генерируется метод билдера.
     * @param name Имя свойства
     * @param type Тип свойства
     * @param setter Имя сеттера
     * @since 1.0
     */
    record Property(String name, String type, String setter) {
    }
}

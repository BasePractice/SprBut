/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m07;

import ru.sprbut.m07.api.GenerateBuilder;
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
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public BuilderProcessor() {
        // нечего инициализировать
    }

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

    /** Счётчик раундов — чтобы было видно, что их несколько (модуль 08). */
    private int round;

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        this.filer = env.getFiler();
        this.messager = env.getMessager();
        this.elements = env.getElementUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        this.round++;
        if (roundEnv.processingOver()) {
            // Последний, «пустой» раунд: генерировать здесь уже нельзя
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                this.error(element, "@GenerateBuilder применим только к классам");
                continue;
            }
            final TypeElement type = (TypeElement) element;
            final List<Property> properties = this.analyze(type);
            if (properties == null) {
                // анализ нашёл ошибки — они уже сообщены, генерировать нечего
                continue;
            }
            this.generate(type, properties);
        }
        // true — «аннотации обработаны мной, другим процессорам не передавать»
        return true;
    }

    /**
     * Раунды обработки.
     * @return Раунды обработки
     */
    public int rounds() {
        return this.round;
    }

    // --- Анализ --------------------------------------------------------------

    /**
     * Проверяет соглашение JavaBeans и собирает свойства.
     * Возвращает {@code null}, если код непригоден — ошибки уже отправлены в Messager.
     * @param type Тип
     * @return Проверяет соглашение JavaBeans и собирает свойства
     */
    private List<Property> analyze(final TypeElement type) {
        boolean valid = true;

        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            this.error(type, "@GenerateBuilder не применим к абстрактному классу");
            valid = false;
        }
        if (!this.hasPublicNoArgConstructor(type)) {
            this.error(type, "Классу нужен публичный конструктор без параметров — "
                    + "билдер создаёт объект именно им");
            valid = false;
        }

        final List<Property> properties = new ArrayList<>();
        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            final Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            final String name = field.getSimpleName().toString();
            final String setter = "set" + capitalize(name);
            if (!this.hasSetter(type, setter)) {
                this.error(field, "Нет сеттера " + setter + "(...) — билдер не сможет задать это поле");
                valid = false;
                continue;
            }
            properties.add(new Property(name, field.asType().toString(), setter));
        }

        if (properties.isEmpty() && valid) {
            this.warning(type, "У класса нет свойств — сгенерированный билдер будет пустым");
        }
        return valid ? properties : null;
    }

    private boolean hasPublicNoArgConstructor(final TypeElement type) {
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(type.getEnclosedElements());
        if (constructors.isEmpty()) {
            // конструктор по умолчанию — публичный, если класс публичный
            return true;
        }
        return constructors.stream().anyMatch(c ->
                c.getParameters().isEmpty() && c.getModifiers().contains(Modifier.PUBLIC));
    }

    private boolean hasSetter(final TypeElement type, final String setterName) {
        return ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .anyMatch(m -> m.getSimpleName().contentEquals(setterName)
                        && m.getParameters().size() == 1
                        && m.getModifiers().contains(Modifier.PUBLIC));
    }

    // --- Генерация -----------------------------------------------------------

    /**
     * Пишет новый исходник через {@link Filer}. Важно: файл создаётся именно
     * так, а не через {@code new FileWriter} — иначе javac не узнает о новом
     * коде и не скомпилирует его в следующем раунде.
     * @param properties Свойства
     * @param type Тип
     */
    private void generate(final TypeElement type, final List<Property> properties) {
        final String packageName = this.elements.getPackageOf(type).getQualifiedName().toString();
        final String simpleName = type.getSimpleName().toString();
        final String suffix = type.getAnnotation(GenerateBuilder.class).suffix();
        final String builderName = simpleName + suffix;
        final String qualifiedName = packageName.isEmpty() ? builderName : packageName + "." + builderName;

        try {
            final JavaFileObject file = this.filer.createSourceFile(qualifiedName, type);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                if (!packageName.isEmpty()) {
                    out.println("package " + packageName + ";");
                    out.println();
                }
                out.println("/** Сгенерирован " + BuilderProcessor.class.getSimpleName()
                        + ". Правки будут потеряны при следующей сборке. */");
                out.println("public final class " + builderName + " {");
                out.println();
                for (Property property : properties) {
                    out.println("    private " + property.type() + " " + property.name() + ";");
                }
                out.println();
                out.println("    private " + builderName + "() {");
                out.println("    }");
                out.println();
                out.println("    public static " + builderName + " create() {");
                out.println("        return new " + builderName + "();");
                out.println("    }");
                for (Property property : properties) {
                    out.println();
                    out.println("    public " + builderName + " " + property.name()
                            + "(" + property.type() + " value) {");
                    out.println("        this." + property.name() + " = value;");
                    out.println("        return this;");
                    out.println("    }");
                }
                out.println();
                out.println("    public " + simpleName + " build() {");
                out.println("        " + simpleName + " result = new " + simpleName + "();");
                for (Property property : properties) {
                    out.println("        result." + property.setter() + "(this." + property.name() + ");");
                }
                out.println("        return result;");
                out.println("    }");
                out.println("}");
            }
        } catch (final IOException e) {
            this.error(type, "Не удалось записать " + qualifiedName + ": " + e.getMessage());
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

    /** Свойство, для которого генерируется метод билдера. */
    record Property(String name, String type, String setter) {
    }
}

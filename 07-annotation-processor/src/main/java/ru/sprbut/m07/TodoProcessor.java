/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import ru.sprbut.m07.api.Todo;

/**
 * Слайд 60: «Анализ исходного кода».
 *
 * <p>Процессор, который <b>ничего не генерирует</b>. Он только проверяет код
 * и печатает диагностику через {@code Messager} — ровно так работают Error Prone,
 * NullAway и проверки Checker Framework.</p>
 *
 * <p>Возвращает {@code false} из {@code process}: «я аннотацию не поглотил,
 * пусть её увидят и другие процессоры». Это важное отличие от
 * {@link BuilderProcessor}, который возвращает {@code true}.</p>
 *
 * @since 1.0
 */
@SupportedAnnotationTypes("ru.sprbut.m07.api.Todo")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TodoProcessor extends AbstractProcessor {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TodoProcessor() {
        // нечего инициализировать
    }

    // false в ответе означает, что аннотацию мы не поглощаем: она может быть
    // нужна и другим процессорам
    @Override
    public final boolean process(
        final Set<? extends TypeElement> annotations, final RoundEnvironment env
    ) {
        if (!env.processingOver()) {
            for (final Element element : env.getElementsAnnotatedWith(Todo.class)) {
                this.report(element);
            }
        }
        return false;
    }

    // одна отметка: блокирующая становится ошибкой компиляции, обычная — предупреждением
    private void report(final Element element) {
        final Todo todo = element.getAnnotation(Todo.class);
        final Diagnostic.Kind kind;
        if (todo.blocking()) {
            kind = Diagnostic.Kind.ERROR;
        } else {
            kind = Diagnostic.Kind.WARNING;
        }
        this.processingEnv.getMessager().printMessage(
            kind,
            String.format("TODO: %s (%s)", todo.value(), element.getSimpleName()),
            element
        );
    }
}

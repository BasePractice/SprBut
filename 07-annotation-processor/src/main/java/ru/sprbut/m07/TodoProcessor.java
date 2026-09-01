/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07;

import ru.sprbut.m07.api.Todo;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

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

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (final Element element : roundEnv.getElementsAnnotatedWith(Todo.class)) {

            final Todo todo = element.getAnnotation(Todo.class);
            final Diagnostic.Kind kind = todo.blocking() ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING;
            processingEnv.getMessager().printMessage(kind,
                    "TODO: " + todo.value() + " (" + element.getSimpleName() + ")", element);
        }
        // false — аннотацию не поглощаем: она может быть нужна и другим процессорам
        return false;
    }
}

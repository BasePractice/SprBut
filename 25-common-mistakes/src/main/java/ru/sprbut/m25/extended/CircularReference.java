/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.extended;

import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import ru.sprbut.m25.Diagnosis;

/**
 * Диагноз для {@code BeanCurrentlyInCreationException}: бин потребовал сам себя
 * через цепочку зависимостей и застрял в полусобранном состоянии.
 * @since 1.0
 */
public final class CircularReference implements Diagnosis {

    /**
     * Исходная ошибка контейнера.
     */
    private final BeanCurrentlyInCreationException cause;

    /**
     * Основной конструктор.
     * @param cause Исходная ошибка контейнера
     */
    public CircularReference(final BeanCurrentlyInCreationException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return String.format(
            "циклическая зависимость замкнулась на бине %s", this.cause.getBeanName()
        );
    }

    @Override
    public String remedy() {
        return "разделить бины, вынеся общую ответственность в третий, либо отложить через @Lazy";
    }
}

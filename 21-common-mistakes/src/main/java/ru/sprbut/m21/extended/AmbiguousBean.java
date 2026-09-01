/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.extended;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз для {@code NoUniqueBeanDefinitionException}: кандидатов больше одного,
 * а точка внедрения не сказала, какой ей нужен.
 * @since 1.0
 */
public final class AmbiguousBean implements Diagnosis {

    /**
     * Исходная ошибка контейнера.
     */
    private final NoUniqueBeanDefinitionException cause;

    /**
     * Основной конструктор.
     * @param cause Исходная ошибка контейнера
     */
    public AmbiguousBean(final NoUniqueBeanDefinitionException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return String.format(
            "на одну точку внедрения нашлось несколько бинов: %s", this.candidates()
        );
    }

    @Override
    public String remedy() {
        return "пометить обычную реализацию @Primary либо назвать нужную через @Qualifier";
    }

    /**
     * Имена бинов-кандидатов через запятую — это и есть первая подсказка,
     * куда смотреть.
     * @return Имена бинов-кандидатов
     */
    public String candidates() {
        final String names;
        if (this.cause.getBeanNamesFound() == null) {
            names = "неизвестно";
        } else {
            names = String.join(", ", this.cause.getBeanNamesFound());
        }
        return names;
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.extended;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз для {@code NoSuchBeanDefinitionException}: запрошенного типа
 * в контейнере нет ни в одном экземпляре.
 * @since 1.0
 */
public final class MissingBean implements Diagnosis {

    /**
     * Исходная ошибка контейнера.
     */
    private final NoSuchBeanDefinitionException cause;

    /**
     * Основной конструктор.
     * @param cause Исходная ошибка контейнера
     */
    public MissingBean(final NoSuchBeanDefinitionException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return String.format("в контексте нет бина типа %s", this.type());
    }

    @Override
    public String remedy() {
        return String.format(
            "объявить @Bean или @Component для %s либо сделать зависимость Optional",
            this.type()
        );
    }

    private String type() {
        final String name;
        if (this.cause.getResolvableType() == null) {
            name = String.valueOf(this.cause.getBeanName());
        } else {
            name = this.cause.getResolvableType().toClass().getSimpleName();
        }
        return name;
    }
}

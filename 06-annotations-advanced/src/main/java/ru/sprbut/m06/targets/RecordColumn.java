/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

import java.lang.reflect.RecordComponent;
import java.util.Optional;

/**
 * Имя колонки, объявленное на компоненте record.
 *
 * <p>Компонент record — самостоятельная сущность рефлексии: не поле, не метод
 * и не параметр конструктора, хотя порождает все три.</p>
 *
 * @since 1.0
 */
public final class RecordColumn {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Компонент.
     */
    private final String component;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param component Компонент
     */
    public RecordColumn(final Class<?> type, final String component) {
        this.type = type;
        this.component = component;
    }

    /**
     * Имя колонки, если компонент помечен.
     * @return Имя колонки, если компонент помечен
     */
    public Optional<String> name() {
        for (final RecordComponent each : this.type.getRecordComponents()) {
            if (each.getName().equals(this.component)) {
                return Optional.ofNullable(each.getAnnotation(Column.class)).map(Column::name);
            }
        }
        throw new IllegalArgumentException("Нет компонента '" + this.component + "'");
    }
}

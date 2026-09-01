/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;

/**
 * Класс с аннотациями во всех местах, перечисленных на слайде 39.
 *
 * <p>Локальная переменная помечена намеренно: её аннотация вообще не попадает
 * в class-файл, и прочитать её в runtime невозможно в принципе.
 * {@code LOCAL_VARIABLE} существует только для инструментов уровня исходников.</p>
 *
 * @since 1.0
 */
@Marker
@Level("класс")
@SuppressWarnings("unused")
public class Annotated {
    /**
     * Основной конструктор.
     */
    public Annotated() {
        // тело намеренно пустое
    }

    /**
     * Поле.
     */
    @Level("поле")
    private String field;

    /**
     * Метод.
     * @param parameter Значение {@code parameter}
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @Marker
    @Level("метод")
    public void method(final String parameter) {
        @SuppressWarnings("unused")
        final String local = parameter;
    }
}

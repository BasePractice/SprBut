/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Одно правило проверки: смотрит на поле и его значение, возвращает нарушения.
 *
 * <p>Правил на поле может сработать несколько, поэтому возвращается список,
 * а не одно нарушение: три несоблюдённых шаблона — это три сообщения,
 * а не одно, выбранное наугад.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Rule {

    /**
     * Нарушения, найденные в значении этого поля; пустой список, если всё в порядке.
     * @param field Поле
     * @param value Значение
     * @return Нарушения, найденные в значении этого поля; пустой список, если всё в порядке
     */
    List<Violation> check(Field field, Object value);
}

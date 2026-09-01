/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Audited;

/**
 * Родитель с аннотацией на классе и на методе.
 *
 * <p>На классе аннотация унаследуется, на методе — нет. Разница видна
 * в {@link Child}.</p>
 *
 * @since 1.0
 */
@Audited(actor = "родитель")
public class Parent {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Parent() {
        // нечего инициализировать
    }

    /**
     * Действие, помеченное аудитом.
     * @return Действие, помеченное аудитом
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @Audited(actor = "метод-родителя")
    public String action() {
        return "parent";
    }
}

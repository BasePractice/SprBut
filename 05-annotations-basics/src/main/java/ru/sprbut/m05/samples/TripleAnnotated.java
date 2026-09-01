/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Retentions;

/**
 * Класс, на каждом элементе которого стоят аннотации всех трёх политик хранения.
 *
 * <p>В исходниках их четыре, в байткоде — две, в runtime видна ровно одна.
 * Наглядная иллюстрация того, что «написано в коде» и «доступно рефлексии» —
 * разные множества.</p>
 *
 * @since 1.0
 */
@Retentions.SourceLevel
@Retentions.ClassLevel
@Retentions.RuntimeLevel
@Retentions.DefaultRetention
@SuppressWarnings("unused")
public class TripleAnnotated {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TripleAnnotated() {
        // нечего инициализировать
    }

    /**
     * Поле.
     */
    @Retentions.SourceLevel
    @Retentions.ClassLevel
    @Retentions.RuntimeLevel
    private String field;

    /**
     * Метод.
     */
    @Retentions.SourceLevel
    @Retentions.ClassLevel
    @Retentions.RuntimeLevel
    public void method() {
    }
}

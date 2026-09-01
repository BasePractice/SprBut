/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m10.lombok.samples;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code @Getter}/{@code @Setter} по отдельности, с контролем доступа.
 *
 * <p>Показывает, что Lombok — не «всё или ничего»: уровень доступа задаётся
 * и на классе, и на поле, причём поле перекрывает класс.</p>
 *
 * @since 1.0
 */
@Getter
@Setter(AccessLevel.PROTECTED)
public class Partial {

    /**
     * Значение {@code visible}.
     */
    private String visible;

    /**
     * Поле, у которого геттер отключён.
     */
    @Getter(AccessLevel.NONE)
    private String hidden;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Partial() {
        this.hidden = "не виден снаружи";
    }

    /**
     * Единственный способ прочитать поле, у которого геттер отключён.
     * @return Значение скрытого поля
     */
    public String peekHidden() {
        return this.hidden;
    }
}

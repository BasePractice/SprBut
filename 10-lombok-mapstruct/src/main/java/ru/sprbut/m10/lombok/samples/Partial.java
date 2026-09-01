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
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Partial() {
        // нечего инициализировать
    }

    /**
     * Единственный способ прочитать поле, у которого геттер отключён.
     * @return Единственный способ прочитать поле, у которого геттер отключён
     */
    public String peekHidden() {
        return this.hidden;
    }

    /**
     * Значение {@code hidden}.
     */
    @Getter(AccessLevel.NONE)
    private String hidden = "не виден снаружи";
}

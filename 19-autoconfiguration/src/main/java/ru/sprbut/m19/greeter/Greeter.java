/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19.greeter;

/**
 * Контракт «библиотеки», ради которой пишется стартер.
 *
 * <p>Смысл автоконфигурации именно в этом: пользователь подключает зависимость,
 * а рабочий бин появляется сам — с разумными значениями по умолчанию
 * и возможностью всё переопределить.</p>
 *
 * @since 1.0
 */
public interface Greeter {

    /**
     * Значение {@code greet}.
     * @param name Имя
     * @return Значение {@code greet}
     */
    String greet(String name);

    /**
     * Значение {@code flavour}.
     * @return Значение {@code flavour}
     */
    String flavour();
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Интерфейс, вокруг которого строится прокси.
 *
 * <p>{@link #greetTwice} объявлен здесь не для удобства: он вызывает соседний
 * метод через {@code this}, и на нём видно ограничение self-invocation.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Greeter {

    /**
     * Приветствие по имени.
     * @param name Имя
     * @return Приветствие по имени
     */
    String greet(String name);

    /**
     * Длина текста.
     * @param text Текст
     * @return Длина текста
     */
    int length(String text);

    /**
     * Двойное приветствие — реализовано через вызов соседнего метода.
     * @param name Имя
     * @return Двойное приветствие — реализовано через вызов соседнего метода
     */
    default String greetTwice(final String name) {
        return this.greet(name) + " " + this.greet(name);
    }
}

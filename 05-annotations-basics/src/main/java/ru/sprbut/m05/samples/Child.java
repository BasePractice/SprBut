/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

/**
 * Аннотация класса видна здесь благодаря {@code @Inherited},
 * а аннотация переопределённого метода — нет: методы не наследуют аннотации никогда.
 * @since 1.0
 */
public class Child extends Parent {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Child() {
        // нечего инициализировать
    }

    @Override
    public final String action() {
        return "child";
    }
}

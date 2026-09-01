/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Что можно узнать об объекте, если он оказался прокси.
 *
 * <p>Полезно при отладке: в стектрейсах Spring такие классы называются
 * {@code $Proxy17}, и понять по имени, что это за бин, невозможно —
 * а через обработчик можно добраться до настоящей цели.</p>
 *
 * @since 1.0
 */
public final class ProxyFacts {

    /**
     * Значение {@code candidate}.
     */
    private final Object candidate;

    /**
     * Основной конструктор.
     * @param candidate Значение {@code candidate}
     */
    public ProxyFacts(final Object candidate) {
        this.candidate = candidate;
    }

    /**
     * Сгенерирован ли этот класс механизмом JDK-прокси.
     * @return Сгенерирован ли этот класс механизмом JDK-прокси
     */
    public boolean generated() {
        return Proxy.isProxyClass(this.candidate.getClass());
    }

    /**
     * Обработчик, стоящий за прокси.
     * @return Обработчик, стоящий за прокси
     */
    public InvocationHandler handler() {
        return Proxy.getInvocationHandler(this.candidate);
    }
}

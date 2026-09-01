/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Записывает факт вызова, после чего управление уходит в оригинальный метод.
 *
 * <p>{@code @This} даёт ссылку на сам сгенерированный объект — по его имени видно,
 * что это подкласс, а не исходный класс.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class LoggingInterceptor {

    private LoggingInterceptor() {
    }

    /**
     * Перехват перед вызовом оригинала.
     * @param self Ссылка на себя
     */
    public static void before(final @This Object self) {
        Intercepted.add(String.format("вызов у %s", self.getClass().getSimpleName()));
    }
}

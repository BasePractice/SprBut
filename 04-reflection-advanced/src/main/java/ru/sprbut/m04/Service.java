/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Носитель точек внедрения: конструктор и метод-сеттер, оба с параметрами
 * помеченными и непомеченными.
 *
 * <p>Нужен, чтобы у {@link Parameters} было что разбирать, — и заодно показывает,
 * что контейнеру безразлично, где находится точка внедрения.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class Service {

    /**
     * Имя.
     */
    private final String name;

    /**
     * Число повторов.
     */
    private final int retries;

    /**
     * Основной конструктор.
     * @param @Injected("appName" Имя
     */
    public Service(final @Injected("appName") String name, final @Injected int retries, final boolean debug) {
        this.name = name;
        this.retries = retries;
    }

    /**
     * Настройка через метод — вторая разновидность точки внедрения.
     * @param lines Значение {@code lines}
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public void configure(final @Injected("timeout") long millis, final String label) {
        // параметры разбираются рефлексией, тело здесь роли не играет
    }
}

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
     * Основной конструктор: часть параметров помечена для внедрения,
     * часть — намеренно нет, именно это и разбирает рефлексия.
     * @param name Имя
     * @param retries Число повторов
     * @param debug Признак отладки, ничем не помеченный
     */
    public Service(
        @Injected("appName") final String name,
        @Injected final int retries,
        final boolean debug
    ) {
        this.name = name;
        this.retries = retries;
    }

    /**
     * Настройка через метод — вторая разновидность точки внедрения.
     * Тело роли не играет: параметры разбираются рефлексией.
     * @param millis Таймаут
     * @param label Метка
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public void configure(@Injected("timeout") final long millis, final String label) {
        // параметры разбираются рефлексией, тело здесь роли не играет
    }
}

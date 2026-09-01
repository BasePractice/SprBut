/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19.greeter;

import java.util.Locale;

/**
 * Реализация по умолчанию, которую поставляет автоконфигурация.
 * @since 1.0
 */
public final class SimpleGreeter implements Greeter {

    /**
     * Шаблон.
     */
    private final String template;

    /**
     * Громкое сообщение.
     */
    private final boolean shout;

    /**
     * Основной конструктор.
     * @param template Шаблон
     * @param shout Громкое сообщение
     */
    public SimpleGreeter(final String template, final boolean shout) {
        this.template = template;
        this.shout = shout;
    }

    @Override
    public String greet(final String name) {
        final String message = this.template.replace("{name}", name);
        final String shown;
        if (this.shout) {
            shown = message.toUpperCase(Locale.ROOT);
        } else {
            shown = message;
        }
        return shown;
    }

    @Override
    public String flavour() {
        return "simple";
    }
}

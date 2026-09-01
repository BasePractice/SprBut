/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m22.reflection;

/**
 * Расширение, которое забыли объявить в {@link ru.sprbut.m22.hints.PluginHints}.
 *
 * <p>На JVM неотличимо от {@link CsvPlugin} — тесты зелёные, приложение работает.
 * Разница проявится только в native image и только в рантайме:
 * {@code ClassNotFoundException} на классе, который лежит в исходниках.</p>
 *
 * @since 1.0
 */
public final class JsonPlugin implements Plugin {

    /**
     * Открытый конструктор: образ создаёт расширение рефлексией.
     */
    public JsonPlugin() {
        // ничего не инициализируем
    }

    @Override
    public String name() {
        return "json";
    }
}

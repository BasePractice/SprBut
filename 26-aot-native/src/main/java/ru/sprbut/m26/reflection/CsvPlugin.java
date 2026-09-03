/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m26.reflection;

/**
 * Расширение, которое загружается по имени класса и никем не вызывается напрямую.
 *
 * <p>На JVM это работает всегда. В native image — только если класс объявлен
 * в {@code RuntimeHints}: иначе ни самого класса, ни его конструктора в образе нет.</p>
 *
 * @since 1.0
 */
public final class CsvPlugin implements Plugin {

    /**
     * Открытый конструктор: образ создаёт расширение рефлексией.
     */
    public CsvPlugin() {
        // нечего инициализировать
    }

    @Override
    public String name() {
        return "csv";
    }
}

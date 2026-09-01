/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

/**
 * Результат выполнения команды вместе с расшифровкой того, что было выбрано.
 *
 * <p>Выбранный конструктор и подпись метода в отчёте не для красоты: когда движок
 * из нескольких перегрузок берёт не ту, единственный способ это увидеть —
 * прочитать, что именно он выбрал.</p>
 *
 * @param type        имя класса, экземпляр которого создан
 * @param constructor описание выбранного конструктора
 * @param signature   подпись выбранного метода
 * @param result      то, что метод вернул
 * @since 1.0
 */
public record Invocation(String type, String constructor, String signature, Object result) {
}

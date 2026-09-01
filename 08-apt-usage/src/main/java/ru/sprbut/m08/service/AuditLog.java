/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.service;

import java.util.ArrayList;
import java.util.List;
import ru.sprbut.m07.api.Registered;
import ru.sprbut.m07.api.Todo;

/**
 * Третий участник реестра, он же носитель {@code @Todo} — при сборке модуля
 * javac напечатает предупреждение от {@code TodoProcessor}.
 * @since 1.0
 */
@Registered("audit")
public class AuditLog {

    /**
     * Записи.
     */
    private final List<String> entries;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AuditLog() {
        this.entries = new ArrayList<>(0);
    }

    /**
     * Записи.
     * @return Записи
     */
    public List<String> entries() {
        return List.copyOf(this.entries);
    }

    /**
     * Значение {@code record}.
     * @param event Событие
     */
    @Todo("заменить на структурированное логирование")
    public void record(final String event) {
        this.entries.add(event);
    }
}

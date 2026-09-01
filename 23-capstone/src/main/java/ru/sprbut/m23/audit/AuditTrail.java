/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * Журнал аудита — singleton, который переживает все запросы.
 *
 * <p>{@code CopyOnWriteArrayList} выбран не случайно: бин один на всё приложение,
 * а писать в него будут потоки веб-сервера. Обычный {@code ArrayList} здесь
 * тихо ломается под нагрузкой — классическая цена того, что областью видимости
 * по умолчанию является singleton.</p>
 *
 * @since 1.0
 */
@Component
public final class AuditTrail {

    /**
     * Записи.
     */
    private final List<String> records;

    /**
     * Основной конструктор.
     */
    public AuditTrail() {
        this(new CopyOnWriteArrayList<>());
    }

    /**
     * Основной конструктор.
     * @param records Записи
     */
    public AuditTrail(final List<String> records) {
        this.records = records;
    }

    /**
     * Добавляет запись об операции.
     * @param operation Значение {@code operation}
     */
    public void record(final String operation) {
        this.records.add(operation);
    }

    /**
     * Снимок журнала на текущий момент.
     * @return Снимок журнала на текущий момент
     */
    public List<String> records() {
        return List.copyOf(this.records);
    }
}

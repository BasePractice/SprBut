/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.circular;

/**
 * Вторая половина цикла: книга проводок спрашивает счета о сумме.
 *
 * <p>Обращение к {@link Invoices} отложено до вызова {@link #entries()} — именно
 * поэтому {@code @Lazy}-прокси спасает положение: на этапе сборки контекста
 * настоящий объект ещё не нужен.</p>
 *
 * @since 1.0
 */
public final class LedgerService implements Ledger {

    /**
     * Счета, по которым сверяется книга.
     */
    private final Invoices invoices;

    /**
     * Основной конструктор.
     * @param invoices Счета, по которым сверяется книга
     */
    public LedgerService(final Invoices invoices) {
        this.invoices = invoices;
    }

    @Override
    public int entries() {
        return 3;
    }

    /**
     * Сверка книги со счетами — здесь прокси наконец разворачивается в бин.
     * @return Признак того, что книга сходится
     */
    public boolean balanced() {
        return this.invoices.total() == this.entries() * 100;
    }
}

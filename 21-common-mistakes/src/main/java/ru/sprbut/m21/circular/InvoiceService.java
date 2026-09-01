/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.circular;

/**
 * Одна половина цикла: счета не умеют считать итог без книги проводок.
 * @since 1.0
 */
public final class InvoiceService implements Invoices {

    /**
     * Книга записей, из которой считается сумма.
     */
    private final Ledger ledger;

    /**
     * Основной конструктор.
     * @param ledger Книга записей
     */
    public InvoiceService(final Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public int total() {
        return this.ledger.entries() * 100;
    }
}

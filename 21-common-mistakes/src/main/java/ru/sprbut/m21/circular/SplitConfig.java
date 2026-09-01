/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.circular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Честное лечение цикла: разделить бины.
 *
 * <p>Взаимная зависимость почти всегда означает, что два класса делят одну
 * ответственность. Здесь она вынесена наружу — {@code ledger} больше не знает
 * о счетах, и цикл исчезает не потому, что его спрятали, а потому,
 * что его не стало.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class SplitConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SplitConfig() {
        // нечего инициализировать
    }

    /**
     * Книга записей без зависимостей — цикл разорван.
     * @return Книга записей
     */
    @Bean
    public Ledger ledger() {
        return () -> 3;
    }

    /**
     * Счета, собранные поверх книги записей.
     * @param ledger Книга записей
     * @return Счета
     */
    @Bean
    public Invoices invoices(final Ledger ledger) {
        return new InvoiceService(ledger);
    }
}

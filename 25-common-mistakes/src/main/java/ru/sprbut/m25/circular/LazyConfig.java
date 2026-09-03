/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m25.circular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Лечение цикла отсрочкой: {@code @Lazy} на одной из двух точек внедрения.
 *
 * <p>Вместо настоящего бина {@code ledger} получает JDK-прокси, который найдёт
 * {@link Invoices} в контейнере при первом вызове метода. К этому моменту
 * контекст уже собран, цикла нет.</p>
 *
 * <p>Это обезболивающее, а не лечение: взаимная зависимость никуда не делась,
 * её просто перестало быть видно на старте. Честный выход — {@link SplitConfig}.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class LazyConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LazyConfig() {
        // нечего инициализировать
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

    /**
     * Книга записей, получающая счета отложенно через прокси.
     * @param invoices Счета, внедряемые через прокси
     * @return Книга записей
     */
    @Bean
    public Ledger ledger(final @Lazy Invoices invoices) {
        return new LedgerService(invoices);
    }
}

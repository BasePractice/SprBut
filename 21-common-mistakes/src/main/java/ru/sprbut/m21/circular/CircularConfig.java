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
 * Слайд «Типичные ошибки»: circular reference и {@code BeanCurrentlyInCreationException}.
 *
 * <p>Два бина требуют друг друга через конструктор. Контейнер начинает собирать
 * {@code invoices}, обнаруживает зависимость от {@code ledger}, идёт собирать его,
 * снова упирается в недособранный {@code invoices} — и останавливается,
 * потому что отдать наполовину созданный объект он не может.</p>
 *
 * <p>Через поля или сеттеры цикл бы «сработал»: Spring подставил бы недоинициализированную
 * ссылку. Конструктор делает проблему видимой на старте — это его достоинство,
 * а не недостаток. Начиная с Boot 2.6 циклы запрещены и по умолчанию.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class CircularConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CircularConfig() {
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
     * Книга записей, знающая про счета — вторая половина цикла.
     * @param invoices Счета
     * @return Книга записей
     */
    @Bean
    public Ledger ledger(final Invoices invoices) {
        return new LedgerService(invoices);
    }
}

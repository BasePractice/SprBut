/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.missing;

import org.springframework.stereotype.Service;

/**
 * Слайд «Типичные ошибки»: {@code NoSuchBeanDefinitionException} — бин не найден.
 *
 * <p>Сервис требует {@link PaymentGateway} через конструктор. Пока в контексте нет
 * ни одной реализации, контейнер падает на этапе создания бина, а не при вызове
 * метода — это и есть главное отличие DI от {@code new}: ошибка проводки
 * обнаруживается на старте, а не в проде под нагрузкой.</p>
 *
 * @since 1.0
 */
@Service
public final class CheckoutService {

    /**
     * Платёжный шлюз, которого может не оказаться в контексте.
     */
    private final PaymentGateway gateway;

    /**
     * Основной конструктор.
     * @param gateway Платёжный шлюз
     */
    public CheckoutService(final PaymentGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Проводит оплату через внедрённый канал.
     * @return Отчёт об оплате
     */
    public String pay() {
        return String.format("оплата через %s", this.gateway.channel());
    }
}

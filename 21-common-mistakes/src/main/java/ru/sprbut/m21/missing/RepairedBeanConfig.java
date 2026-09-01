/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.missing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Та же конфигурация, но с недостающим бином.
 *
 * <p>Разница с {@link MissingBeanConfig} — ровно один метод {@code @Bean}.
 * Контекст поднимается, {@link CheckoutService} получает свой канал.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(CheckoutService.class)
public final class RepairedBeanConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RepairedBeanConfig() {
        // нечего инициализировать
    }

    /**
     * Недостающий шлюз, объявленный явно.
     * @return Платёжный шлюз
     */
    @Bean
    public PaymentGateway gateway() {
        return new CardGateway();
    }
}

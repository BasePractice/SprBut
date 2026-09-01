/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.missing;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация, которой не хватает одного бина.
 *
 * <p>{@link CheckoutService} импортирован, {@link PaymentGateway} — нет. Контекст
 * не поднимется: {@code NoSuchBeanDefinitionException} с текстом
 * «expected at least 1 bean which qualifies as autowire candidate».</p>
 *
 * <p>Лечится тремя способами: объявить бин, поставить {@code @ComponentScan} на пакет
 * с реализацией или сделать зависимость необязательной через {@code Optional}.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(CheckoutService.class)
public final class MissingBeanConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MissingBeanConfig() {
        // нечего инициализировать
    }
}

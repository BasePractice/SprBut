/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.ambiguous;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Первое лечение неоднозначности: {@code @Primary}.
 *
 * <p>Выбор по умолчанию задаётся на стороне объявления бина — все точки внедрения
 * без уточнения получат {@code express}. Подходит, когда одна реализация
 * действительно «обычная», а остальные — исключения.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(DeliveryService.class)
public final class PrimaryConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public PrimaryConfig() {
        // нечего инициализировать
    }

    /**
     * Быстрая служба доставки.
     * @return Служба доставки за один день
     */
    @Bean
    @Primary
    public Shipper express() {
        return () -> 1;
    }

    /**
     * Экономная служба доставки.
     * @return Служба доставки за неделю
     */
    @Bean
    public Shipper economy() {
        return () -> 7;
    }
}

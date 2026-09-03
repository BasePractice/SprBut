/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m25.ambiguous;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Второе лечение неоднозначности: {@code @Qualifier}.
 *
 * <p>Бины остаются равноправными, ни один не помечен главным — выбор делает
 * {@link EconomyDelivery} в своём конструкторе. Имя бина по умолчанию равно
 * имени {@code @Bean}-метода, поэтому квалификатором служит {@code economy}.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(EconomyDelivery.class)
public final class QualifierConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public QualifierConfig() {
        // нечего инициализировать
    }

    /**
     * Быстрая служба доставки.
     * @return Служба доставки за один день
     */
    @Bean
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

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
 * Слайд «Типичные ошибки»: {@code NoUniqueBeanDefinitionException}.
 *
 * <p>Два бина одного типа и точка внедрения без уточнения. Контейнер отбирает
 * кандидатов по типу, находит двух и честно отказывается угадывать:
 * «expected single matching bean but found 2: express,economy».</p>
 *
 * <p>Важная деталь: пока {@link Shipper} никто не просит, контекст здоров.
 * Ошибка рождается не из объявления бинов, а из точки внедрения.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(DeliveryService.class)
public final class AmbiguousConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AmbiguousConfig() {
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

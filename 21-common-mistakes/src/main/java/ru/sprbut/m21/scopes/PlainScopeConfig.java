/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Слайд «Типичные ошибки»: prototype внутри singleton без {@code proxyMode}.
 *
 * <p>{@link Ticket} объявлен прототипом, но {@link Gate} получает его один раз
 * при своей сборке. Каждый вызов {@code admit()} возвращает один и тот же номер:
 * область видимости определяется не аннотацией, а тем, кто и когда просит бин.</p>
 *
 * <p>Ошибка коварна тем, что контекст поднимается без единой жалобы —
 * неверным оказывается только поведение.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class PlainScopeConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public PlainScopeConfig() {
        // нечего инициализировать
    }

    /**
     * Счётчик номеров, общий для всех талонов.
     * @return Счётчик номеров
     */
    @Bean
    public Serial serial() {
        return new Serial();
    }

    /**
     * Singleton, держащий талон.
     * @param ticket Талон
     * @return Проходная
     */
    @Bean
    public Gate gate(final Ticket ticket) {
        return new Gate(ticket);
    }

    /**
     * Талон в области видимости prototype.
     * @param serial Счётчик номеров
     * @return Талон
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Ticket ticket(final Serial serial) {
        return new Ticket(serial);
    }
}

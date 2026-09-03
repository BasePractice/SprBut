/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m25.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Лечение: {@code @Scope(proxyMode = TARGET_CLASS)}.
 *
 * <p>В {@link Gate} внедряется не сам {@link Ticket}, а CGLIB-подкласс-заглушка.
 * Она не хранит состояния и на каждый вызов метода идёт в контейнер за свежим
 * экземпляром — поэтому {@code admit()} наконец возвращает разные номера.</p>
 *
 * <p>Альтернатива без прокси — попросить {@code ObjectProvider<Ticket>} и звать
 * {@code getObject()} самому; честнее по зависимостям, многословнее в коде.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class ProxiedScopeConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ProxiedScopeConfig() {
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
    @Scope(
        value = ConfigurableBeanFactory.SCOPE_PROTOTYPE,
        proxyMode = ScopedProxyMode.TARGET_CLASS
    )
    public Ticket ticket(final Serial serial) {
        return new Ticket(serial);
    }
}

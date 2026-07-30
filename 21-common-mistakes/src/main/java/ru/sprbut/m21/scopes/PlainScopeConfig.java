package ru.sprbut.m21.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Слайд «Типичные ошибки»: prototype внутри singleton без {@code proxyMode}.
 * <p>
 * {@link Ticket} объявлен прототипом, но {@link Gate} получает его один раз
 * при своей сборке. Каждый вызов {@code admit()} возвращает один и тот же номер:
 * область видимости определяется не аннотацией, а тем, кто и когда просит бин.
 * <p>
 * Ошибка коварна тем, что контекст поднимается без единой жалобы —
 * неверным оказывается только поведение.
 */
@Configuration(proxyBeanMethods = false)
public final class PlainScopeConfig {

    @Bean
    public Serial serial() {
        return new Serial();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Ticket ticket(Serial serial) {
        return new Ticket(serial);
    }

    @Bean
    public Gate gate(Ticket ticket) {
        return new Gate(ticket);
    }
}

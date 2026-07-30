package ru.sprbut.m21.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Лечение: {@code @Scope(proxyMode = TARGET_CLASS)}.
 * <p>
 * В {@link Gate} внедряется не сам {@link Ticket}, а CGLIB-подкласс-заглушка.
 * Она не хранит состояния и на каждый вызов метода идёт в контейнер за свежим
 * экземпляром — поэтому {@code admit()} наконец возвращает разные номера.
 * <p>
 * Альтернатива без прокси — попросить {@code ObjectProvider<Ticket>} и звать
 * {@code getObject()} самому; честнее по зависимостям, многословнее в коде.
 */
@Configuration(proxyBeanMethods = false)
public final class ProxiedScopeConfig {

    @Bean
    public Serial serial() {
        return new Serial();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Ticket ticket(Serial serial) {
        return new Ticket(serial);
    }

    @Bean
    public Gate gate(Ticket ticket) {
        return new Gate(ticket);
    }
}

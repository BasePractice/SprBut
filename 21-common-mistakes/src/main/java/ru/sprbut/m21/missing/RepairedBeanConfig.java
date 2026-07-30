package ru.sprbut.m21.missing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Та же конфигурация, но с недостающим бином.
 * <p>
 * Разница с {@link MissingBeanConfig} — ровно один метод {@code @Bean}.
 * Контекст поднимается, {@link CheckoutService} получает свой канал.
 */
@Configuration(proxyBeanMethods = false)
@Import(CheckoutService.class)
public final class RepairedBeanConfig {

    @Bean
    public PaymentGateway gateway() {
        return new CardGateway();
    }
}

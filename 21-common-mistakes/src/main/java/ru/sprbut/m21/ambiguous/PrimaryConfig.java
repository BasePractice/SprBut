package ru.sprbut.m21.ambiguous;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Первое лечение неоднозначности: {@code @Primary}.
 * <p>
 * Выбор по умолчанию задаётся на стороне объявления бина — все точки внедрения
 * без уточнения получат {@code express}. Подходит, когда одна реализация
 * действительно «обычная», а остальные — исключения.
 */
@Configuration(proxyBeanMethods = false)
@Import(DeliveryService.class)
public final class PrimaryConfig {

    @Bean
    @Primary
    public Shipper express() {
        return () -> 1;
    }

    @Bean
    public Shipper economy() {
        return () -> 7;
    }
}

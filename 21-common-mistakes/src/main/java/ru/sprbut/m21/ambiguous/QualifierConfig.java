package ru.sprbut.m21.ambiguous;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Второе лечение неоднозначности: {@code @Qualifier}.
 * <p>
 * Бины остаются равноправными, ни один не помечен главным — выбор делает
 * {@link EconomyDelivery} в своём конструкторе. Имя бина по умолчанию равно
 * имени {@code @Bean}-метода, поэтому квалификатором служит {@code economy}.
 */
@Configuration(proxyBeanMethods = false)
@Import(EconomyDelivery.class)
public final class QualifierConfig {

    @Bean
    public Shipper express() {
        return () -> 1;
    }

    @Bean
    public Shipper economy() {
        return () -> 7;
    }
}

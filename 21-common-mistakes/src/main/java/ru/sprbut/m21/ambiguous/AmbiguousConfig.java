package ru.sprbut.m21.ambiguous;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Слайд «Типичные ошибки»: {@code NoUniqueBeanDefinitionException}.
 * <p>
 * Два бина одного типа и точка внедрения без уточнения. Контейнер отбирает
 * кандидатов по типу, находит двух и честно отказывается угадывать:
 * «expected single matching bean but found 2: express,economy».
 * <p>
 * Важная деталь: пока {@link Shipper} никто не просит, контекст здоров.
 * Ошибка рождается не из объявления бинов, а из точки внедрения.
 */
@Configuration(proxyBeanMethods = false)
@Import(DeliveryService.class)
public final class AmbiguousConfig {

    @Bean
    public Shipper express() {
        return () -> 1;
    }

    @Bean
    public Shipper economy() {
        return () -> 7;
    }
}

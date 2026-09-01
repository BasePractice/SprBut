/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m13.qualifiers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.List;
import java.util.Map;

/**
 * Слайды 102–103: {@code @Autowired}, {@code @Qualifier}, {@code @Primary}.
 *
 * <p>Когда бинов одного типа несколько, контейнер обязан понять, какой брать.
 * Порядок разрешения такой:
 * <ol>
 * <li>единственный кандидат — берём его;</li>
 * <li>есть {@code @Qualifier} в точке внедрения — берём названный;</li>
 * <li>есть {@code @Primary} — берём его;</li>
 * <li>имя параметра совпадает с именем бина — берём по имени;</li>
 * <li>иначе — {@code NoUniqueBeanDefinitionException} (модуль 21).</li>
 * </ol></p>
 *
 * @since 1.0
 */
@Configuration
public class QualifierConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public QualifierConfig() {
        // нечего инициализировать
    }

    /**
     * Шлюз.
     * @since 1.0
     */
    public interface PaymentGateway {
        /**
         * Значение {@code pay}.
         * @param amount Сумма
         * @return Значение {@code pay}
         */
        String pay(String amount);

        /**
         * Имя.
         * @return Имя
         */
        String name();
    }

    /**
     * Шлюз.
     * @param name Имя
     * @return Шлюз
     */
    public record NamedGateway(String name) implements PaymentGateway {
        @Override
        public String pay(final String amount) {
            return this.name + ":" + amount;
        }
    }

    /**
     * Шлюз.
     * @return Шлюз
     */
    @Bean
    @Primary
    public PaymentGateway cardGateway() {
        return new NamedGateway("card");
    }

    /**
     * Шлюз.
     * @return Шлюз
     */
    @Bean
    public PaymentGateway cashGateway() {
        return new NamedGateway("cash");
    }

    /**
     * Шлюз.
     * @return Шлюз
     */
    @Bean
    @Qualifier("fast")
    public PaymentGateway sbpGateway() {
        return new NamedGateway("sbp");
    }

    /**
     * Без уточнений сработает {@code @Primary}.
     * @param gateway Шлюз
     * @return Без уточнений сработает {@code @Primary}
     */
    @Bean
    public PrimaryConsumer primaryConsumer(final PaymentGateway gateway) {
        return new PrimaryConsumer(gateway);
    }

    /**
     * {@code @Qualifier} по имени бина перебивает {@code @Primary}.
     * @param gateway Шлюз
     * @return {@code @Qualifier} по имени бина перебивает {@code @Primary}
     */
    @Bean
    public QualifiedConsumer qualifiedConsumer(final @Qualifier("cashGateway") PaymentGateway gateway) {
        return new QualifiedConsumer(gateway);
    }

    /**
     * {@code @Qualifier} по значению аннотации, а не по имени бина.
     * @param gateway Шлюз
     * @return {@code @Qualifier} по значению аннотации, а не по имени бина
     */
    @Bean
    public TaggedConsumer taggedConsumer(final @Qualifier("fast") PaymentGateway gateway) {
        return new TaggedConsumer(gateway);
    }

    /**
     * Особый случай: контейнер умеет внедрять <b>все</b> бины типа сразу —
     * списком или картой «имя бина → бин». {@code @Primary} тут не участвует.
     * @param all Все элементы
     * @param byName Имя
     * @return Особый случай: контейнер умеет внедрять <b>все</b> бины типа сразу — списком или картой «имя бина → бин». {@code @Primary} тут не участвует
     */
    @Bean
    public GatewayRegistry gatewayRegistry(final List<PaymentGateway> all, final Map<String, PaymentGateway> byName) {
        return new GatewayRegistry(all, byName);
    }

    /**
     * Потребитель главной реализации.
     * @param gateway Шлюз
     * @return Потребитель главной реализации
     */
    public record PrimaryConsumer(PaymentGateway gateway) {
    }

    /**
     * Потребитель с квалификатором.
     * @param gateway Шлюз
     * @return Потребитель с квалификатором
     */
    public record QualifiedConsumer(PaymentGateway gateway) {
    }

    /**
     * Потребитель с меткой.
     * @param gateway Шлюз
     * @return Потребитель с меткой
     */
    public record TaggedConsumer(PaymentGateway gateway) {
    }

    /**
     * Шлюз.
     * @param all Все элементы
     * @param byName Имя
     * @return Шлюз
     */
    public record GatewayRegistry(List<PaymentGateway> all, Map<String, PaymentGateway> byName) {
    }
}

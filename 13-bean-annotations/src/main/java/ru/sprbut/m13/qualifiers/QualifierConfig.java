package ru.sprbut.m13.qualifiers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;

/**
 * Слайды 102–103: {@code @Autowired}, {@code @Qualifier}, {@code @Primary}.
 * <p>
 * Когда бинов одного типа несколько, контейнер обязан понять, какой брать.
 * Порядок разрешения такой:
 * <ol>
 *   <li>единственный кандидат — берём его;</li>
 *   <li>есть {@code @Qualifier} в точке внедрения — берём названный;</li>
 *   <li>есть {@code @Primary} — берём его;</li>
 *   <li>имя параметра совпадает с именем бина — берём по имени;</li>
 *   <li>иначе — {@code NoUniqueBeanDefinitionException} (модуль 21).</li>
 * </ol>
 */
@Configuration
public class QualifierConfig {

    public interface PaymentGateway {
        String pay(String amount);

        String name();
    }

    public record NamedGateway(String name) implements PaymentGateway {
        @Override
        public String pay(String amount) {
            return name + ":" + amount;
        }
    }

    @Bean
    @Primary
    public PaymentGateway cardGateway() {
        return new NamedGateway("card");
    }

    @Bean
    public PaymentGateway cashGateway() {
        return new NamedGateway("cash");
    }

    @Bean
    @Qualifier("fast")
    public PaymentGateway sbpGateway() {
        return new NamedGateway("sbp");
    }

    /** Без уточнений сработает {@code @Primary}. */
    @Bean
    public PrimaryConsumer primaryConsumer(PaymentGateway gateway) {
        return new PrimaryConsumer(gateway);
    }

    /** {@code @Qualifier} по имени бина перебивает {@code @Primary}. */
    @Bean
    public QualifiedConsumer qualifiedConsumer(@Qualifier("cashGateway") PaymentGateway gateway) {
        return new QualifiedConsumer(gateway);
    }

    /** {@code @Qualifier} по значению аннотации, а не по имени бина. */
    @Bean
    public TaggedConsumer taggedConsumer(@Qualifier("fast") PaymentGateway gateway) {
        return new TaggedConsumer(gateway);
    }

    /**
     * Особый случай: контейнер умеет внедрять <b>все</b> бины типа сразу —
     * списком или картой «имя бина → бин». {@code @Primary} тут не участвует.
     */
    @Bean
    public GatewayRegistry gatewayRegistry(List<PaymentGateway> all, Map<String, PaymentGateway> byName) {
        return new GatewayRegistry(all, byName);
    }

    public record PrimaryConsumer(PaymentGateway gateway) {
    }

    public record QualifiedConsumer(PaymentGateway gateway) {
    }

    public record TaggedConsumer(PaymentGateway gateway) {
    }

    public record GatewayRegistry(List<PaymentGateway> all, Map<String, PaymentGateway> byName) {
    }
}

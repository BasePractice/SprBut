package ru.sprbut.m11.step3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import ru.sprbut.m11.step2.ManualOrderService;

import java.math.BigDecimal;

/**
 * Слайд 87: «пример — управление DI Spring».
 * <p>
 * Тот же граф, что в {@link ru.sprbut.m11.step2.ObjectFactory}, но собирает его
 * контейнер. Ключевая разница видна в методе {@link #orderService}: аргументы
 * <b>не создаются вручную</b> — контейнер подбирает их по типу и подставляет сам.
 * <p>
 * Что мы получили сверх ручной фабрики:
 * <ul>
 *   <li>синглтоны и их создание — забота контейнера, а не наша;</li>
 *   <li>порядок создания вычисляется автоматически по графу зависимостей;</li>
 *   <li>появился жизненный цикл: инициализация, уничтожение, точки расширения
 *       (модуль 14).</li>
 * </ul>
 * Это и есть смысл слайда 84: «IoC-контейнер отвечает за создание объектов
 * и управление их жизненным циклом».
 */
@Configuration
public class SpringWiringConfig {

    @Bean
    public NotificationSender notificationSender() {
        return new EmailSender();
    }

    @Bean
    public PriceCalculator priceCalculator() {
        return new PriceCalculator(new BigDecimal("0.20"));
    }

    /**
     * Аргументы метода — это точки внедрения. Контейнер найдёт подходящие бины
     * по типу и передаст их сюда. Ни одного {@code new} для зависимостей.
     */
    @Bean
    public ManualOrderService orderService(NotificationSender sender, PriceCalculator calculator) {
        return new ManualOrderService(sender, calculator);
    }
}

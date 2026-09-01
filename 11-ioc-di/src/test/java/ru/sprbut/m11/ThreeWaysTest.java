/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import ru.sprbut.m11.domain.SmsSender;
import ru.sprbut.m11.step1.HardcodedOrderService;
import ru.sprbut.m11.step2.ManualOrderService;
import ru.sprbut.m11.step2.ObjectFactory;
import ru.sprbut.m11.step3.SpringWiringConfig;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Слайды 83–88 (СХЕМА 5): от new к контейнеру.
 * @since 1.0
 */
@DisplayName("Слайды 83–88 (СХЕМА 5): от new к контейнеру")
final class ThreeWaysTest {

    @Nested
/**
 * Шаг 1: зависимости создаются внутри.
 * @since 1.0
 */
    @DisplayName("Шаг 1: зависимости создаются внутри")
    final class Hardcoded {

        @Test
        @DisplayName("Работает, но реализацию подменить нечем")
        void worksButIsRigid() {
            final HardcodedOrderService service = new HardcodedOrderService();
            MatcherAssert.assertThat(
                "hardcoded service cannot place the order",
                service.placeOrder("ivanov@mail.ru", new BigDecimal("100")),
                Matchers.comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Чтобы что-то проверить, пришлось добавить геттер ради теста")
        void testabilityRequiresProductionCodeChanges() {
            final HardcodedOrderService service = new HardcodedOrderService();
            service.placeOrder("ivanov@mail.ru", new BigDecimal("100"));
            MatcherAssert.assertThat(
                "testability cannot demand a getter added for the test",
                service.senderForTests().sent(),
                Matchers.contains("ivanov@mail.ru <- Заказ на сумму 120.00")
            );
        }

        @Test
        @DisplayName("Каждый экземпляр сервиса плодит свои копии зависимостей")
        void everyInstanceDuplicatesItsDependencies() {
            MatcherAssert.assertThat(
                "every instance cannot duplicate its dependencies",
                new HardcodedOrderService().senderForTests(),
                Matchers.not(Matchers.sameInstance(new HardcodedOrderService().senderForTests()))
            );
        }

        @Test
        @DisplayName("Сигнатура молчит о зависимостях — их видно только из тела класса")
        void dependenciesAreInvisibleFromOutside() {
            MatcherAssert.assertThat(
                "constructor cannot stay silent about the dependencies",
                HardcodedOrderService.class.getConstructors()[0].getParameterCount(),
                Matchers.equalTo(0)
            );
        }
    }

    @Nested
/**
 * Шаг 2: ручное управление.
 * @since 1.0
 */
    @DisplayName("Шаг 2: ручное управление")
    final class Manual {

        @Test
        @DisplayName("Зависимости приходят снаружи — реализация подменяется в одну строку")
        void dependenciesAreInjectable() {
            final SmsSender sms = new SmsSender();
            final ManualOrderService service = new ManualOrderService(
                    sms, new PriceCalculator(
                        new BigDecimal(
                            "0.20"
                        )
                    ));
            service.placeOrder("+79001234567", new BigDecimal("100"));
            MatcherAssert.assertThat(
                "injected implementation cannot be swapped in one line",
                service.usedChannel(),
                Matchers.equalTo("sms")
            );
        }

        @Test
        @DisplayName("Конструктор — честный список того, без чего объект не работает")
        void constructorDocumentsDependencies() {
            MatcherAssert.assertThat(
                "constructor cannot document the dependencies honestly",
                ManualOrderService.class.getConstructors()[0].getParameterTypes(),
                Matchers.arrayContaining(NotificationSender.class, PriceCalculator.class)
            );
        }

        @Test
        @DisplayName("Фабрика собирает граф и хранит синглтоны")
        void factoryAssemblesAndCaches() {
            final ObjectFactory factory = new ObjectFactory("email");
            final ManualOrderService first = factory.orderService();
            final ManualOrderService second = factory.orderService();
            MatcherAssert.assertThat(
                "factory cannot cache the assembled singleton",
                first,
                Matchers.sameInstance(second)
            );
        }

        @Test
        @DisplayName("фабрика собирает весь граф целиком")
        void factoryAssemblesWholeGraph() {
            MatcherAssert.assertThat(
                "factory cannot assemble the whole graph",
                new ObjectFactory("email").notificationSender(),
                Matchers.instanceOf(EmailSender.class)
            );
        }

        @Test
        @DisplayName("Выбор реализации перенесён в одну точку — в фабрику")
        void factoryDecidesTheImplementation() {
            MatcherAssert.assertThat(
                "factory cannot concentrate the implementation choice",
                new ObjectFactory("sms").orderService().usedChannel(),
                Matchers.equalTo("sms")
            );
        }
    }

    @Nested
/**
 * Шаг 3: управление DI Spring.
 * @since 1.0
 */
    @DisplayName("Шаг 3: управление DI Spring")
    final class SpringManaged {

        @Test
        @DisplayName("Контейнер сам подбирает аргументы по типу — ни одного new для зависимостей")
        void containerResolvesArgumentsByType() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {

                final ManualOrderService service = context.getBean(ManualOrderService.class);
                MatcherAssert.assertThat(
                    "container cannot resolve the arguments by type",
                    service.placeOrder("ivanov@mail.ru", new BigDecimal("100")),
                    Matchers.comparesEqualTo(new BigDecimal("120.00"))
                );
            }
        }

        @Test
        @DisplayName("Бины по умолчанию — синглтоны")
        void beansAreSingletonsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {

                MatcherAssert.assertThat(
                    "beans cannot be singletons by default",
                    context.getBean(ManualOrderService.class),
                    Matchers.sameInstance(context.getBean(ManualOrderService.class))
                );
            }
        }

        @Test
        @DisplayName("Порядок создания вычисляется по графу, а не задаётся руками")
        void creationOrderIsDerivedFromTheGraph() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                // orderService не мог быть создан раньше своих зависимостей
                final ManualOrderService service = context.getBean(ManualOrderService.class);
                MatcherAssert.assertThat(
                    "creation order cannot be derived from the graph",
                    service,
                    Matchers.notNullValue()
                );
            }
        }

        @Test
        @DisplayName("Контейнер закрывается — и вместе с ним заканчивается жизненный цикл бинов")
        void containerOwnsTheLifecycle() {
            final AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(
                        SpringWiringConfig.class
                    );
            context.close();
            MatcherAssert.assertThat(
                "closed container cannot end the lifecycle of its beans",
                context.isActive(),
                Matchers.equalTo(false)
            );
        }
    }
}

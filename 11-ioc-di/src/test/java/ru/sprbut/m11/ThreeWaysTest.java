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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

@DisplayName("Слайды 83–88 (СХЕМА 5): от new к контейнеру")
final class ThreeWaysTest {

    @Nested
    @DisplayName("Шаг 1: зависимости создаются внутри")
    class Hardcoded {

        @Test
        @DisplayName("Работает, но реализацию подменить нечем")
        void worksButIsRigid() {
            HardcodedOrderService service = new HardcodedOrderService();

            assertThat(
                "hardcoded service cannot place the order",
                service.placeOrder("ivanov@mail.ru", new BigDecimal("100")),
                comparesEqualTo(new BigDecimal("120.00"))
            );
        }

        @Test
        @DisplayName("Чтобы что-то проверить, пришлось добавить геттер ради теста")
        void testabilityRequiresProductionCodeChanges() {
            HardcodedOrderService service = new HardcodedOrderService();
            service.placeOrder("ivanov@mail.ru", new BigDecimal("100"));

            assertThat(
                "testability cannot demand a getter added for the test",
                service.senderForTests().sent(),
                contains("ivanov@mail.ru <- Заказ на сумму 120.00")
            );
        }

        @Test
        @DisplayName("Каждый экземпляр сервиса плодит свои копии зависимостей")
        void everyInstanceDuplicatesItsDependencies() {
            assertThat(
                "every instance cannot duplicate its dependencies",
                new HardcodedOrderService().senderForTests(),
                not(sameInstance(new HardcodedOrderService().senderForTests()))
            );
        }

        @Test
        @DisplayName("Сигнатура молчит о зависимостях — их видно только из тела класса")
        void dependenciesAreInvisibleFromOutside() {
            assertThat(
                "constructor cannot stay silent about the dependencies",
                HardcodedOrderService.class.getConstructors()[0].getParameterCount(),
                equalTo(0)
            );
        }
    }

    @Nested
    @DisplayName("Шаг 2: ручное управление")
    class Manual {

        @Test
        @DisplayName("Зависимости приходят снаружи — реализация подменяется в одну строку")
        void dependenciesAreInjectable() {
            SmsSender sms = new SmsSender();
            ManualOrderService service = new ManualOrderService(
                    sms, new PriceCalculator(new BigDecimal("0.20")));

            service.placeOrder("+79001234567", new BigDecimal("100"));

            assertThat(
                "injected implementation cannot be swapped in one line",
                service.usedChannel(),
                equalTo("sms")
            );
        }

        @Test
        @DisplayName("Конструктор — честный список того, без чего объект не работает")
        void constructorDocumentsDependencies() {
            assertThat(
                "constructor cannot document the dependencies honestly",
                ManualOrderService.class.getConstructors()[0].getParameterTypes(),
                arrayContaining(NotificationSender.class, PriceCalculator.class)
            );
        }

        @Test
        @DisplayName("Фабрика собирает граф и хранит синглтоны")
        void factoryAssemblesAndCaches() {
            ObjectFactory factory = new ObjectFactory("email");

            ManualOrderService first = factory.orderService();
            ManualOrderService second = factory.orderService();

            assertThat(
                "factory cannot cache the assembled singleton",
                first,
                sameInstance(second)
            );
        }

        @Test
        @DisplayName("фабрика собирает весь граф целиком")
        void factoryAssemblesWholeGraph() {
            assertThat(
                "factory cannot assemble the whole graph",
                new ObjectFactory("email").notificationSender(),
                instanceOf(EmailSender.class)
            );
        }

        @Test
        @DisplayName("Выбор реализации перенесён в одну точку — в фабрику")
        void factoryDecidesTheImplementation() {
            assertThat(
                "factory cannot concentrate the implementation choice",
                new ObjectFactory("sms").orderService().usedChannel(),
                equalTo("sms")
            );
        }
    }

    @Nested
    @DisplayName("Шаг 3: управление DI Spring")
    class SpringManaged {

        @Test
        @DisplayName("Контейнер сам подбирает аргументы по типу — ни одного new для зависимостей")
        void containerResolvesArgumentsByType() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                ManualOrderService service = context.getBean(ManualOrderService.class);

                assertThat(
                    "container cannot resolve the arguments by type",
                    service.placeOrder("ivanov@mail.ru", new BigDecimal("100")),
                    comparesEqualTo(new BigDecimal("120.00"))
                );
            }
        }

        @Test
        @DisplayName("Бины по умолчанию — синглтоны")
        void beansAreSingletonsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                assertThat(
                    "beans cannot be singletons by default",
                    context.getBean(ManualOrderService.class),
                    sameInstance(context.getBean(ManualOrderService.class))
                );
            }
        }

        @Test
        @DisplayName("Порядок создания вычисляется по графу, а не задаётся руками")
        void creationOrderIsDerivedFromTheGraph() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                // orderService не мог быть создан раньше своих зависимостей
                ManualOrderService service = context.getBean(ManualOrderService.class);

                assertThat(
                    "creation order cannot be derived from the graph",
                    service,
                    notNullValue()
                );
            }
        }

        @Test
        @DisplayName("Контейнер закрывается — и вместе с ним заканчивается жизненный цикл бинов")
        void containerOwnsTheLifecycle() {
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(SpringWiringConfig.class);
            context.close();
            assertThat(
                "closed container cannot end the lifecycle of its beans",
                context.isActive(),
                equalTo(false)
            );
        }
    }
}

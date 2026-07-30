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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 83–88 (СХЕМА 5): от new к контейнеру")
class ThreeWaysTest {

    @Nested
    @DisplayName("Шаг 1: зависимости создаются внутри")
    class Hardcoded {

        @Test
        @DisplayName("Работает, но реализацию подменить нечем")
        void worksButIsRigid() {
            HardcodedOrderService service = new HardcodedOrderService();

            assertThat(service.placeOrder("ivanov@mail.ru", new BigDecimal("100")))
                    .isEqualByComparingTo("120.00");
        }

        @Test
        @DisplayName("Чтобы что-то проверить, пришлось добавить геттер ради теста")
        void testabilityRequiresProductionCodeChanges() {
            HardcodedOrderService service = new HardcodedOrderService();
            service.placeOrder("ivanov@mail.ru", new BigDecimal("100"));

            assertThat(service.senderForTests().sent())
                    .containsExactly("ivanov@mail.ru <- Заказ на сумму 120.00");
        }

        @Test
        @DisplayName("Каждый экземпляр сервиса плодит свои копии зависимостей")
        void everyInstanceDuplicatesItsDependencies() {
            assertThat(new HardcodedOrderService().senderForTests())
                    .isNotSameAs(new HardcodedOrderService().senderForTests());
        }

        @Test
        @DisplayName("Сигнатура молчит о зависимостях — их видно только из тела класса")
        void dependenciesAreInvisibleFromOutside() {
            assertThat(HardcodedOrderService.class.getConstructors())
                    .singleElement()
                    .satisfies(c -> assertThat(c.getParameterCount()).isZero());
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

            assertThat(service.usedChannel()).isEqualTo("sms");
            assertThat(sms.sent()).hasSize(1);
        }

        @Test
        @DisplayName("Конструктор — честный список того, без чего объект не работает")
        void constructorDocumentsDependencies() {
            assertThat(ManualOrderService.class.getConstructors())
                    .singleElement()
                    .satisfies(c -> assertThat(c.getParameterTypes())
                            .containsExactly(NotificationSender.class, PriceCalculator.class));
        }

        @Test
        @DisplayName("Фабрика собирает граф и хранит синглтоны")
        void factoryAssemblesAndCaches() {
            ObjectFactory factory = new ObjectFactory("email");

            ManualOrderService first = factory.orderService();
            ManualOrderService second = factory.orderService();

            assertThat(first).isSameAs(second);
            assertThat(factory.notificationSender()).isInstanceOf(EmailSender.class);
            assertThat(factory.createdCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Выбор реализации перенесён в одну точку — в фабрику")
        void factoryDecidesTheImplementation() {
            assertThat(new ObjectFactory("sms").orderService().usedChannel()).isEqualTo("sms");
            assertThat(new ObjectFactory("email").orderService().usedChannel()).isEqualTo("email");
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

                assertThat(service.usedChannel()).isEqualTo("email");
                assertThat(service.placeOrder("ivanov@mail.ru", new BigDecimal("100")))
                        .isEqualByComparingTo("120.00");
            }
        }

        @Test
        @DisplayName("Бины по умолчанию — синглтоны")
        void beansAreSingletonsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                assertThat(context.getBean(ManualOrderService.class))
                        .isSameAs(context.getBean(ManualOrderService.class));
                assertThat(context.getBean(NotificationSender.class))
                        .isSameAs(context.getBean("notificationSender"));
            }
        }

        @Test
        @DisplayName("Порядок создания вычисляется по графу, а не задаётся руками")
        void creationOrderIsDerivedFromTheGraph() {
            try (var context = new AnnotationConfigApplicationContext(SpringWiringConfig.class)) {
                // orderService не мог быть создан раньше своих зависимостей
                ManualOrderService service = context.getBean(ManualOrderService.class);

                assertThat(service).isNotNull();
                assertThat(context.getBeanDefinitionNames())
                        .contains("notificationSender", "priceCalculator", "orderService");
            }
        }

        @Test
        @DisplayName("Контейнер закрывается — и вместе с ним заканчивается жизненный цикл бинов")
        void containerOwnsTheLifecycle() {
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(SpringWiringConfig.class);
            assertThat(context.isActive()).isTrue();

            context.close();

            assertThat(context.isActive()).isFalse();
        }
    }
}

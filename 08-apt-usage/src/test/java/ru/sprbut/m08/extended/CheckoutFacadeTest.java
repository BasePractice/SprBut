package ru.sprbut.m08.extended;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m08.model.Customer;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.service.AuditLog;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: полный цикл APT в рабочем коде")
final class CheckoutFacadeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 7, 30);

    private CustomerRepository customers;
    private OrderRepository orders;
    private AuditLog audit;
    private CheckoutFacade facade;

    @BeforeEach
    void setUp() {
        customers = new CustomerRepository();
        orders = new OrderRepository();
        audit = new AuditLog();
        facade = new CheckoutFacade(customers, orders, audit);
    }

    @Test
    @DisplayName("Регистрация собирает объект сгенерированным билдером")
    void registersCustomer() {
        Customer customer = facade.register("C-1", "Иванов", "ivanov@mail.ru", 42, false);

        assertThat(
            "generated builder cannot assemble the registered customer",
            customer.getName(),
            equalTo("Иванов")
        );
    }

    @Test
    @DisplayName("Оформление заказа использует второй сгенерированный билдер")
    void placesOrder() {
        facade.register("C-1", "Иванов", "ivanov@mail.ru", 42, false);

        Order order = facade.checkout("C-1", new BigDecimal("1000"), DAY);

        assertThat(
            "second generated builder cannot assemble the order",
            order.getTotal(),
            comparesEqualTo(new BigDecimal("1000"))
        );
    }

    @Test
    @DisplayName("VIP получает скидку — бизнес-логика поверх сгенерированного кода")
    void vipGetsDiscount() {
        facade.register("C-2", "Петров", "petrov@mail.ru", 35, true);

        Order order = facade.checkout("C-2", new BigDecimal("1000"), DAY);

        assertThat(
            "vip discount cannot be applied on top of the generated code",
            order.getTotal(),
            comparesEqualTo(new BigDecimal("900.0"))
        );
    }

    @Test
    @DisplayName("Номера заказов нумеруются подряд")
    void numbersOrdersSequentially() {
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);

        facade.checkout("C-1", BigDecimal.TEN, DAY);
        facade.checkout("C-1", BigDecimal.ONE, DAY);

        assertThat(
            "orders cannot be numbered sequentially",
            facade.ordersOf("C-1").stream().map(Order::getNumber).toList(),
            contains("ORD-1", "ORD-2")
        );
    }

    @Test
    @DisplayName("Неизвестный покупатель отклоняется")
    void rejectsUnknownCustomer() {
        assertThat(
            "unknown customer cannot be rejected with an explanation",
            assertThrows(
                IllegalArgumentException.class,
                () -> facade.checkout("нет-такого", BigDecimal.TEN, DAY)
            ).getMessage(),
            containsString("Нет покупателя")
        );
    }

    @Test
    @DisplayName("Аудит фиксирует все шаги")
    void auditTrailIsRecorded() {
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);
        facade.checkout("C-1", new BigDecimal("500"), DAY);

        assertThat(
            "audit cannot record every step",
            facade.auditTrail(),
            contains("зарегистрирован C-1", "заказ ORD-1 на 500")
        );
    }

    @Test
    @DisplayName("Конструктор без аргументов достаёт зависимости из сгенерированного реестра")
    void resolvesDependenciesFromGeneratedRegistry() {
        CheckoutFacade fromRegistry = new CheckoutFacade();

        fromRegistry.register("C-9", "Сидоров", "s@mail.ru", 30, false);
        Order order = fromRegistry.checkout("C-9", new BigDecimal("250"), DAY);

        assertThat(
            "generated registry cannot supply the dependencies",
            order.getCustomerId(),
            equalTo("C-9")
        );
    }

    @Test
    @DisplayName("Реестр знает все три компонента модуля")
    void registryKnowsEveryComponent() {
        assertThat(
            "registry cannot know every component of the module",
            CheckoutFacade.registeredNames(),
            containsInAnyOrder("customers", "orderRepository", "audit")
        );
    }
}

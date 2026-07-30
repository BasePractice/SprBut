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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: полный цикл APT в рабочем коде")
class CheckoutFacadeTest {

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

        assertThat(customer.getName()).isEqualTo("Иванов");
        assertThat(customer.getBalance()).isEqualByComparingTo("0");
        assertThat(customers.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Оформление заказа использует второй сгенерированный билдер")
    void placesOrder() {
        facade.register("C-1", "Иванов", "ivanov@mail.ru", 42, false);

        Order order = facade.checkout("C-1", new BigDecimal("1000"), DAY);

        assertThat(order.getNumber()).isEqualTo("ORD-1");
        assertThat(order.getTotal()).isEqualByComparingTo("1000");
        assertThat(order.getStatus()).isEqualTo("NEW");
        assertThat(order.getPlacedOn()).isEqualTo(DAY);
    }

    @Test
    @DisplayName("VIP получает скидку — бизнес-логика поверх сгенерированного кода")
    void vipGetsDiscount() {
        facade.register("C-2", "Петров", "petrov@mail.ru", 35, true);

        Order order = facade.checkout("C-2", new BigDecimal("1000"), DAY);

        assertThat(order.getTotal()).isEqualByComparingTo("900.0");
    }

    @Test
    @DisplayName("Номера заказов нумеруются подряд")
    void numbersOrdersSequentially() {
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);

        facade.checkout("C-1", BigDecimal.TEN, DAY);
        facade.checkout("C-1", BigDecimal.ONE, DAY);

        assertThat(facade.ordersOf("C-1"))
                .extracting(Order::getNumber)
                .containsExactly("ORD-1", "ORD-2");
    }

    @Test
    @DisplayName("Неизвестный покупатель отклоняется")
    void rejectsUnknownCustomer() {
        assertThatThrownBy(() -> facade.checkout("нет-такого", BigDecimal.TEN, DAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нет покупателя");
    }

    @Test
    @DisplayName("Аудит фиксирует все шаги")
    void auditTrailIsRecorded() {
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);
        facade.checkout("C-1", new BigDecimal("500"), DAY);

        assertThat(facade.auditTrail())
                .containsExactly("зарегистрирован C-1", "заказ ORD-1 на 500");
    }

    @Test
    @DisplayName("Конструктор без аргументов достаёт зависимости из сгенерированного реестра")
    void resolvesDependenciesFromGeneratedRegistry() {
        CheckoutFacade fromRegistry = new CheckoutFacade();

        fromRegistry.register("C-9", "Сидоров", "s@mail.ru", 30, false);
        Order order = fromRegistry.checkout("C-9", new BigDecimal("250"), DAY);

        assertThat(order.getCustomerId()).isEqualTo("C-9");
        assertThat(fromRegistry.auditTrail()).hasSize(2);
    }

    @Test
    @DisplayName("Реестр знает все три компонента модуля")
    void registryKnowsEveryComponent() {
        assertThat(CheckoutFacade.registeredNames())
                .containsExactlyInAnyOrder("customers", "orderRepository", "audit");
    }
}

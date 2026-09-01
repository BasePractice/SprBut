/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m08.extended;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.service.AuditLog;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;

/**
 * Расширенный пример: полный цикл APT в рабочем коде.
 * @since 1.0
 */
@DisplayName("Расширенный пример: полный цикл APT в рабочем коде")
@SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
final class CheckoutFacadeTest {

    /**
     * Значение {@code DAY}.
     */
    private static final LocalDate DAY = LocalDate.of(2026, 7, 30);

    @Test
    @DisplayName("Регистрация собирает объект сгенерированным билдером")
    void registersCustomer() {
        MatcherAssert.assertThat(
            "generated builder cannot assemble the registered customer",
            CheckoutFacadeTest.facade()
                .register("C-1", "Иванов", "ivanov@mail.ru", 42, false)
                .getName(),
            Matchers.equalTo("Иванов")
        );
    }

    @Test
    @DisplayName("Оформление заказа использует второй сгенерированный билдер")
    void placesOrder() {
        final CheckoutFacade facade = CheckoutFacadeTest.facade();
        facade.register("C-1", "Иванов", "ivanov@mail.ru", 42, false);
        MatcherAssert.assertThat(
            "second generated builder cannot assemble the order",
            facade.checkout("C-1", new BigDecimal("1000"), DAY).getTotal(),
            Matchers.comparesEqualTo(new BigDecimal("1000"))
        );
    }

    @Test
    @DisplayName("VIP получает скидку — бизнес-логика поверх сгенерированного кода")
    void vipGetsDiscount() {
        final CheckoutFacade facade = CheckoutFacadeTest.facade();
        facade.register("C-2", "Петров", "petrov@mail.ru", 35, true);
        MatcherAssert.assertThat(
            "vip discount cannot be applied on top of the generated code",
            facade.checkout("C-2", new BigDecimal("1000"), DAY).getTotal(),
            Matchers.comparesEqualTo(new BigDecimal("900.0"))
        );
    }

    @Test
    @DisplayName("Номера заказов нумеруются подряд")
    void numbersOrdersSequentially() {
        final CheckoutFacade facade = CheckoutFacadeTest.facade();
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);
        facade.checkout("C-1", BigDecimal.TEN, DAY);
        facade.checkout("C-1", BigDecimal.ONE, DAY);
        MatcherAssert.assertThat(
            "orders cannot be numbered sequentially",
            facade.ordersOf("C-1").stream().map(Order::getNumber).toList(),
            Matchers.contains("ORD-1", "ORD-2")
        );
    }

    @Test
    @DisplayName("Неизвестный покупатель отклоняется")
    void rejectsUnknownCustomer() {
        final CheckoutFacade facade = CheckoutFacadeTest.facade();
        MatcherAssert.assertThat(
            "unknown customer cannot be rejected with an explanation",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> facade.checkout("нет-такого", BigDecimal.TEN, DAY)
            ).getMessage(),
            Matchers.containsString("Нет покупателя")
        );
    }

    @Test
    @DisplayName("Аудит фиксирует все шаги")
    void auditTrailIsRecorded() {
        final CheckoutFacade facade = CheckoutFacadeTest.facade();
        facade.register("C-1", "Иванов", "i@mail.ru", 42, false);
        facade.checkout("C-1", new BigDecimal("500"), DAY);
        MatcherAssert.assertThat(
            "audit cannot record every step",
            facade.auditTrail(),
            Matchers.contains("зарегистрирован C-1", "заказ ORD-1 на 500")
        );
    }

    @Test
    @DisplayName("Конструктор без аргументов достаёт зависимости из сгенерированного реестра")
    void resolvesDependenciesFromGeneratedRegistry() {
        final CheckoutFacade registry = new CheckoutFacade();
        registry.register("C-9", "Сидоров", "s@mail.ru", 30, false);
        MatcherAssert.assertThat(
            "generated registry cannot supply the dependencies",
            registry.checkout("C-9", new BigDecimal("250"), DAY).getCustomerId(),
            Matchers.equalTo("C-9")
        );
    }

    @Test
    @DisplayName("Реестр знает все три компонента модуля")
    void registryKnowsEveryComponent() {
        MatcherAssert.assertThat(
            "registry cannot know every component of the module",
            CheckoutFacadeTest.facade().registeredNames(),
            Matchers.containsInAnyOrder("customers", "orderRepository", "audit")
        );
    }

    private static CheckoutFacade facade() {
        return new CheckoutFacade(
            new CustomerRepository(), new OrderRepository(), new AuditLog()
        );
    }
}

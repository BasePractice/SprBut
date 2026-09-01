/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// customerId — имя поля сгенерированного билдера, менять его нельзя
// @checkstyle ParameterNameCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.extended;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import ru.sprbut.m08.generated.ModuleRegistry;
import ru.sprbut.m08.model.Customer;
import ru.sprbut.m08.model.CustomerBuilder;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.model.OrderMaker;
import ru.sprbut.m08.service.AuditLog;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;

/**
 * <b>Расширенный пример модуля 08.</b>
 *
 * <p>Полный цикл APT в работе. В этом файле <b>три</b> класса, которых нет
 * в исходниках проекта вообще:
 * <ul>
 * <li>{@code CustomerBuilder} — сгенерирован из {@code @GenerateBuilder};</li>
 * <li>{@code OrderMaker} — тот же процессор, но с другим суффиксом имени;</li>
 * <li>{@code ModuleRegistry} — собран JavaPoet'ом из всех {@code @Registered},
 * в пакет и с именем, заданными через {@code -Aregistry.package} и
 * {@code -Aregistry.class} в pom.xml.</li>
 * </ul>
 * IDE и компилятор видят их как обычные классы: подсказки, проверка типов,
 * переход к определению — всё работает. Разница только в том, что исходник
 * лежит в {@code target/generated-sources/annotations}, а не в {@code src}.</p>
 *
 * <p>Важное свойство: <b>ни одной строчки рефлексии</b>. Зависимости берутся
 * из реестра, который знает конструкторы статически — поэтому такой подход
 * переживает компиляцию в native image (модуль 22).</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.OnlyOneConstructorShouldDoInitialization")
public final class CheckoutFacade {

    /**
     * Клиенты.
     */
    private final CustomerRepository customers;

    /**
     * Заказы.
     */
    private final OrderRepository orders;

    /**
     * Аудит.
     */
    private final AuditLog audit;

    /**
     * Зависимости достаются из сгенерированного реестра по имени.
     *
     * <p>Это compile-time аналог {@code applicationContext.getBean("customers")} —
     * только список бинов известен уже на этапе сборки, а значит вызовы
     * реестра в конструкторе тут и есть предмет разговора.</p>
     *
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public CheckoutFacade() {
        this.customers = (CustomerRepository) ModuleRegistry.create("customers");
        this.orders = (OrderRepository) ModuleRegistry.create("orderRepository");
        this.audit = (AuditLog) ModuleRegistry.create("audit");
    }

    /**
     * Вариант с явным внедрением — для тестов, где нужны свои экземпляры.
     * @param customers Клиенты
     * @param orders Заказы
     * @param audit Аудит
     * @checkstyle ConstructorsOrderCheck (8 lines)
     */
    public CheckoutFacade(final CustomerRepository customers, final OrderRepository orders,
        final AuditLog audit) {
        this.customers = customers;
        this.orders = orders;
        this.audit = audit;
    }

    /**
     * Регистрирует покупателя. Объект собирается сгенерированным билдером —
     * ни одного вызова сеттера в этом коде нет.
     * @param id Идентификатор
     * @param name Имя
     * @param email Адрес почты
     * @param age Возраст
     * @param vip Признак привилегированного клиента
     * @return Зарегистрированный покупатель
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Customer register(
        final String id, final String name, final String email, final int age, final boolean vip
    ) {
        final Customer customer = CustomerBuilder.create()
            .id(id)
            .name(name)
            .email(email)
            .age(age)
            .vip(vip)
            .balance(BigDecimal.ZERO)
            .build();
        this.customers.save(customer);
        this.audit.record(String.format("зарегистрирован %s", id));
        return customer;
    }

    /**
     * Оформляет заказ. Билдер называется {@code OrderMaker} — суффикс задан
     * элементом аннотации {@code @GenerateBuilder(suffix = "Maker")}.
     * @param customerId Идентификатор
     * @param total Итоговая сумма
     * @param date Дата
     * @return Оформленный заказ
     */
    public Order checkout(final String customerId, final BigDecimal total, final LocalDate date) {
        final Customer customer = this.customers.findById(customerId).orElseThrow(
            () -> new IllegalArgumentException(
                String.format("Нет покупателя %s", customerId)
            )
        );
        final BigDecimal charged;
        if (customer.isVip()) {
            charged = total.multiply(new BigDecimal("0.9"));
        } else {
            charged = total;
        }
        final Order order = OrderMaker.create()
            .number(String.format("ORD-%s", this.orders.count() + 1))
            .customerId(customerId)
            .total(charged)
            .placedOn(date)
            .status("NEW")
            .build();
        this.orders.save(order);
        this.audit.record(
            String.format("заказ %s на %s", order.getNumber(), charged)
        );
        return order;
    }

    /**
     * Заказы клиента.
     * @param customerId Идентификатор
     * @return Заказы клиента
     */
    public List<Order> ordersOf(final String customerId) {
        return this.orders.findByCustomer(customerId);
    }

    /**
     * Журнал аудита.
     * @return Журнал аудита
     */
    public List<String> auditTrail() {
        return this.audit.entries();
    }

    /**
     * Что вообще есть в сгенерированном реестре.
     * @return Имена, объявленные в сгенерированном реестре
     * @checkstyle NonStaticMethodCheck (4 lines)
     */
    public Set<String> registeredNames() {
        return ModuleRegistry.names();
    }
}

package ru.sprbut.m08.extended;

import ru.sprbut.m08.generated.ModuleRegistry;
import ru.sprbut.m08.model.Customer;
import ru.sprbut.m08.model.CustomerBuilder;
import ru.sprbut.m08.model.Order;
import ru.sprbut.m08.model.OrderMaker;
import ru.sprbut.m08.service.AuditLog;
import ru.sprbut.m08.service.CustomerRepository;
import ru.sprbut.m08.service.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <b>Расширенный пример модуля 08.</b>
 * <p>
 * Полный цикл APT в работе. В этом файле <b>три</b> класса, которых нет
 * в исходниках проекта вообще:
 * <ul>
 *   <li>{@code CustomerBuilder} — сгенерирован из {@code @GenerateBuilder};</li>
 *   <li>{@code OrderMaker} — тот же процессор, но с другим суффиксом имени;</li>
 *   <li>{@code ModuleRegistry} — собран JavaPoet'ом из всех {@code @Registered},
 *       в пакет и с именем, заданными через {@code -Aregistry.package} и
 *       {@code -Aregistry.class} в pom.xml.</li>
 * </ul>
 * IDE и компилятор видят их как обычные классы: подсказки, проверка типов,
 * переход к определению — всё работает. Разница только в том, что исходник
 * лежит в {@code target/generated-sources/annotations}, а не в {@code src}.
 * <p>
 * Важное свойство: <b>ни одной строчки рефлексии</b>. Зависимости берутся
 * из реестра, который знает конструкторы статически — поэтому такой подход
 * переживает компиляцию в native image (модуль 22).
 */
public final class CheckoutFacade {

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final AuditLog audit;

    /**
     * Зависимости достаются из сгенерированного реестра по имени.
     * Это compile-time аналог {@code applicationContext.getBean("customers")} —
     * только список бинов известен уже на этапе сборки.
     */
    public CheckoutFacade() {
        this.customers = (CustomerRepository) ModuleRegistry.create("customers");
        this.orders = (OrderRepository) ModuleRegistry.create("orderRepository");
        this.audit = (AuditLog) ModuleRegistry.create("audit");
    }

    /** Вариант с явным внедрением — для тестов, где нужны свои экземпляры. */
    public CheckoutFacade(CustomerRepository customers, OrderRepository orders, AuditLog audit) {
        this.customers = customers;
        this.orders = orders;
        this.audit = audit;
    }

    /**
     * Регистрирует покупателя. Объект собирается сгенерированным билдером —
     * ни одного вызова сеттера в этом коде нет.
     */
    public Customer register(String id, String name, String email, int age, boolean vip) {
        Customer customer = CustomerBuilder.create()
                .id(id)
                .name(name)
                .email(email)
                .age(age)
                .vip(vip)
                .balance(BigDecimal.ZERO)
                .build();
        customers.save(customer);
        audit.record("зарегистрирован " + id);
        return customer;
    }

    /**
     * Оформляет заказ. Билдер называется {@code OrderMaker} — суффикс задан
     * элементом аннотации {@code @GenerateBuilder(suffix = "Maker")}.
     */
    public Order checkout(String customerId, BigDecimal total, LocalDate date) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Нет покупателя " + customerId));

        BigDecimal finalTotal = customer.isVip()
                ? total.multiply(new BigDecimal("0.9"))
                : total;

        Order order = OrderMaker.create()
                .number("ORD-" + (orders.count() + 1))
                .customerId(customerId)
                .total(finalTotal)
                .placedOn(date)
                .status("NEW")
                .build();

        orders.save(order);
        audit.record("заказ " + order.getNumber() + " на " + finalTotal);
        return order;
    }

    public List<Order> ordersOf(String customerId) {
        return orders.findByCustomer(customerId);
    }

    public List<String> auditTrail() {
        return audit.entries();
    }

    /** Что вообще есть в сгенерированном реестре. */
    public static java.util.Set<String> registeredNames() {
        return ModuleRegistry.names();
    }
}

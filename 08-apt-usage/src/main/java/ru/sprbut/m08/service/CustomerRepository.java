/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.service;

import ru.sprbut.m07.api.Registered;
import ru.sprbut.m08.model.Customer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Помечен {@code @Registered} — значит попадёт в сгенерированный
 * {@code ModuleRegistry} на этапе компиляции.
 *
 * <p>Сравните с {@code @Component}: там список бинов собирается сканированием
 * classpath при старте приложения, здесь — компилятором при сборке.</p>
 *
 * @since 1.0
 */
@Registered("customers")
public class CustomerRepository {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CustomerRepository() {
        // нечего инициализировать
    }

    /**
     * Хранилище.
     */
    private final Map<String, Customer> storage = new LinkedHashMap<>();

    /**
     * Сохранение.
     * @param customer Клиент
     * @return Сохранение
     */
    public Customer save(final Customer customer) {
        this.storage.put(customer.getId(), customer);
        return customer;
    }

    /**
     * Идентификатор.
     * @param id Идентификатор
     * @return Идентификатор
     */
    public Optional<Customer> findById(final String id) {
        return Optional.ofNullable(this.storage.get(id));
    }

    /**
     * Количество.
     * @return Количество
     */
    public int count() {
        return this.storage.size();
    }
}

package ru.sprbut.m08.service;

import ru.sprbut.m07.api.Registered;
import ru.sprbut.m08.model.Customer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Помечен {@code @Registered} — значит попадёт в сгенерированный
 * {@code ModuleRegistry} на этапе компиляции.
 * <p>
 * Сравните с {@code @Component}: там список бинов собирается сканированием
 * classpath при старте приложения, здесь — компилятором при сборке.
 */
@Registered("customers")
public class CustomerRepository {

    private final Map<String, Customer> storage = new LinkedHashMap<>();

    public Customer save(Customer customer) {
        storage.put(customer.getId(), customer);
        return customer;
    }

    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public int count() {
        return storage.size();
    }
}

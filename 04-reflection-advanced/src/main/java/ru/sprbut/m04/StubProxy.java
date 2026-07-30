package ru.sprbut.m04;

import java.lang.reflect.Proxy;

/**
 * Прокси вообще без цели: поведение целиком синтезируется из метаданных метода.
 * <p>
 * Так работают репозитории Spring Data — интерфейс есть, реализации нет
 * и никогда не было. Здесь синтез сведён к минимуму: возвращается значение
 * по умолчанию для типа результата.
 */
public final class StubProxy<T> {

    private final Class<T> contract;

    public StubProxy(Class<T> contract) {
        this.contract = contract;
    }

    /**
     * Заглушка, отвечающая значениями по умолчанию.
     */
    @SuppressWarnings("unchecked")
    public T proxy() {
        return (T) Proxy.newProxyInstance(
            this.contract.getClassLoader(),
            new Class<?>[]{this.contract},
            (proxy, method, args) -> new DefaultValue(method.getReturnType()).value()
        );
    }
}

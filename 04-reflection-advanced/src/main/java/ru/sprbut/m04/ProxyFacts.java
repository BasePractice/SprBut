package ru.sprbut.m04;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Что можно узнать об объекте, если он оказался прокси.
 * <p>
 * Полезно при отладке: в стектрейсах Spring такие классы называются
 * {@code $Proxy17}, и понять по имени, что это за бин, невозможно —
 * а через обработчик можно добраться до настоящей цели.
 */
public final class ProxyFacts {

    private final Object candidate;

    public ProxyFacts(Object candidate) {
        this.candidate = candidate;
    }

    /**
     * Сгенерирован ли этот класс механизмом JDK-прокси.
     */
    public boolean generated() {
        return Proxy.isProxyClass(this.candidate.getClass());
    }

    /**
     * Обработчик, стоящий за прокси.
     */
    public InvocationHandler handler() {
        return Proxy.getInvocationHandler(this.candidate);
    }
}

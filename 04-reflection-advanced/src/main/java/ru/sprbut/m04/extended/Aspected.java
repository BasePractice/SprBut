package ru.sprbut.m04.extended;

import java.lang.reflect.Proxy;

/**
 * <b>Расширенный пример модуля 04.</b>
 * <p>
 * Работающий мини-AOP на голом JDK — без Spring и без сторонних библиотек.
 * Собирает вместе всё, что перечислено на слайдах 30–36:
 * <ul>
 *   <li><b>Proxy + InvocationHandler</b> (СХЕМА 2) — точка перехвата;</li>
 *   <li><b>аннотации в runtime</b> — что именно делать с методом;</li>
 *   <li><b>MethodHandle</b> — быстрый вызов цели вместо {@code Method.invoke},
 *       с кэшированием хэндла на метод;</li>
 *   <li><b>self-invocation</b> — то самое ограничение, которое потом объясняет,
 *       почему {@code @Transactional} молча не срабатывает (модуль 15).</li>
 * </ul>
 * Это устройство Spring AOP в миниатюре: класс цели не меняется,
 * поведение живёт в обёртке.
 */
public final class Aspected<T> {

    private final Class<T> contract;

    private final T target;

    private final Journal journal;

    public Aspected(Class<T> contract, T target, Journal journal) {
        this.contract = contract;
        this.target = target;
        this.journal = journal;
    }

    /**
     * Прокси, читающий аннотации методов и применяющий аспекты.
     */
    @SuppressWarnings("unchecked")
    public T proxy() {
        if (!this.contract.isInterface()) {
            throw new IllegalArgumentException(
                this.contract.getSimpleName()
                    + " не интерфейс: JDK-прокси умеет проксировать только интерфейсы"
            );
        }
        return (T) Proxy.newProxyInstance(
            this.contract.getClassLoader(),
            new Class<?>[]{this.contract},
            new AspectHandler(this.target, this.journal)
        );
    }
}

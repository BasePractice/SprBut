package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

/**
 * Слайд 34: «Быстрее: MethodHandles, VarHandle».
 * <p>
 * Хэндлы одного класса. В отличие от {@code Method}, они проверяют доступ
 * <b>один раз при создании</b>, а не на каждом вызове, и JIT умеет их
 * встраивать почти как прямой вызов.
 * <p>
 * Права определяются местом вызова: обычный {@code lookup()} видит приватные
 * члены только своего класса и его nestmate'ов. Для чужих нужен
 * {@link MethodHandles#privateLookupIn} — аналог {@code setAccessible(true)}
 * в мире хэндлов, и ограничения JPMS на него распространяются точно так же.
 */
public final class Handles {

    private final Class<?> owner;

    public Handles(Class<?> owner) {
        this.owner = owner;
    }

    /**
     * Хэндл публичного метода. {@link MethodType} описывает сигнатуру целиком
     * и проверяется строго — ошибиться в типе параметра здесь нельзя.
     */
    public MethodHandle virtual(String name, Class<?> returns, Class<?>... parameters)
        throws ReflectiveOperationException {
        return MethodHandles.lookup()
            .findVirtual(this.owner, name, MethodType.methodType(returns, parameters));
    }

    /**
     * Хэндл приватного метода чужого класса.
     */
    public MethodHandle hidden(String name, Class<?> returns, Class<?>... parameters)
        throws ReflectiveOperationException {
        return MethodHandles.privateLookupIn(this.owner, MethodHandles.lookup())
            .findVirtual(this.owner, name, MethodType.methodType(returns, parameters));
    }

    /**
     * Хэндл конструктора: внутри он зовётся {@code <init>} и возвращает {@code void}.
     */
    public MethodHandle constructor(Class<?>... parameters) throws ReflectiveOperationException {
        return MethodHandles.lookup()
            .findConstructor(this.owner, MethodType.methodType(void.class, parameters));
    }

    /**
     * {@link VarHandle} — то же для полей, но с атомарными операциями:
     * {@code compareAndSet} и {@code getAndAdd}, которых у {@code Field} нет.
     */
    public VarHandle field(String name, Class<?> type) throws ReflectiveOperationException {
        return MethodHandles.privateLookupIn(this.owner, MethodHandles.lookup())
            .findVarHandle(this.owner, name, type);
    }
}

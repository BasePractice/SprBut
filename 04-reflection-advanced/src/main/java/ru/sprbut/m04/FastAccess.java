package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

/**
 * Слайды 33–34: «Рефлексия медленнее прямого вызова. Быстрее: MethodHandles, VarHandle».
 * <p>
 * Разница принципиальная. {@code Method.invoke} каждый раз проверяет доступ,
 * упаковывает аргументы в {@code Object[]} и боксирует примитивы.
 * {@link MethodHandle} проверяет доступ <b>один раз</b>, при получении хэндла,
 * а сам вызов JIT способен заинлайнить почти как обычный — если хэндл лежит
 * в {@code static final} поле.
 * <p>
 * Практическое следствие: искать член класса рефлексией — нормально; вызывать
 * в горячем цикле — нет. Ищите один раз, кэшируйте хэндл.
 */
public final class FastAccess {

    private FastAccess() {
    }

    /**
     * Хэндл публичного метода. {@link MethodType} описывает сигнатуру
     * «возвращаемый тип + типы параметров» и проверяется строго.
     */
    public static MethodHandle virtualMethod(Class<?> owner, String name, Class<?> returnType,
                                             Class<?>... paramTypes) throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.findVirtual(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * Хэндл приватного метода <b>чужого</b> класса.
     * <p>
     * Права {@code lookup()} определяются местом вызова: обычный {@code lookup()}
     * видит приватные члены только своего класса и его nestmate'ов. Для всего
     * остального нужен {@link MethodHandles#privateLookupIn} — это и есть аналог
     * {@code setAccessible(true)} в мире хэндлов, и на него точно так же
     * распространяются ограничения JPMS.
     */
    public static MethodHandle privateMethod(Class<?> owner, String name, Class<?> returnType,
                                             Class<?>... paramTypes) throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
        return lookup.findVirtual(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * Хэндл конструктора: имя метода — {@code <init>}, возвращаемый тип — {@code void}.
     */
    public static MethodHandle constructor(Class<?> owner, Class<?>... paramTypes)
            throws ReflectiveOperationException {
        return MethodHandles.lookup().findConstructor(owner, MethodType.methodType(void.class, paramTypes));
    }

    /**
     * {@link VarHandle} — то же самое для полей. В отличие от {@code Field},
     * умеет атомарные операции: {@code compareAndSet}, {@code getAndAdd}.
     */
    public static VarHandle fieldHandle(Class<?> owner, String name, Class<?> fieldType)
            throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
        return lookup.findVarHandle(owner, name, fieldType);
    }

    /**
     * Адаптация хэндла: {@code asType} приводит сигнатуру, {@code bindTo}
     * зафиксирует получателя. Это делает хэндлы композируемыми — то, чего
     * у {@code Method} нет вовсе.
     */
    public static MethodHandle boundTo(MethodHandle handle, Object receiver) {
        return handle.bindTo(receiver);
    }

    /** Счётчик для демонстрации атомарных операций через VarHandle. */
    public static class Counter {
        @SuppressWarnings("unused")
        private volatile int value;
        private String label = "счётчик";

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public int increment(int delta) {
            value += delta;
            return value;
        }

        @SuppressWarnings("unused")
        private String describe(String prefix) {
            return prefix + ": " + label + "=" + value;
        }
    }
}

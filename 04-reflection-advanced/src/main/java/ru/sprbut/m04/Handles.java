/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

/**
 * Слайд 34: «Быстрее: MethodHandles, VarHandle».
 *
 * <p>Хэндлы одного класса. В отличие от {@code Method}, они проверяют доступ
 * <b>один раз при создании</b>, а не на каждом вызове, и JIT умеет их
 * встраивать почти как прямой вызов.</p>
 *
 * <p>Права определяются местом вызова: обычный {@code lookup()} видит приватные
 * члены только своего класса и его nestmate'ов. Для чужих нужен
 * {@link MethodHandles#privateLookupIn} — аналог {@code setAccessible(true)}
 * в мире хэндлов, и ограничения JPMS на него распространяются точно так же.</p>
 *
 * @since 1.0
 */
public final class Handles {

    /**
     * Владелец.
     */
    private final Class<?> owner;

    /**
     * Основной конструктор.
     * @param owner Владелец
     */
    public Handles(final Class<?> owner) {
        this.owner = owner;
    }

    /**
     * Хэндл публичного метода. {@link MethodType} описывает сигнатуру целиком
     * и проверяется строго — ошибиться в типе параметра здесь нельзя.
     * @param name Имя
     * @param parameters Типы параметров
     * @param returns Возвращаемое значение
     * @return Хэндл публичного метода. {@link MethodType} описывает сигнатуру целиком и проверяется строго — ошибиться в типе параметра здесь нельзя
     */
    public MethodHandle virtual(final String name, final Class<?> returns, final Class<?>... parameters)
        throws ReflectiveOperationException {
        return MethodHandles.lookup()
            .findVirtual(this.owner, name, MethodType.methodType(returns, parameters));
    }

    /**
     * Хэндл приватного метода чужого класса.
     * @param name Имя
     * @param parameters Типы параметров
     * @param returns Возвращаемое значение
     * @return Хэндл приватного метода чужого класса
     */
    public MethodHandle hidden(final String name, final Class<?> returns, final Class<?>... parameters)
        throws ReflectiveOperationException {
        return MethodHandles.privateLookupIn(this.owner, MethodHandles.lookup())
            .findVirtual(this.owner, name, MethodType.methodType(returns, parameters));
    }

    /**
     * Хэндл конструктора: внутри он зовётся {@code <init>} и возвращает {@code void}.
     * @param parameters Типы параметров
     * @return Хэндл конструктора: внутри он зовётся {@code <init>} и возвращает {@code void}
     */
    public MethodHandle constructor(final Class<?>... parameters) throws ReflectiveOperationException {
        return MethodHandles.lookup()
            .findConstructor(this.owner, MethodType.methodType(void.class, parameters));
    }

    /**
     * {@link VarHandle} — то же для полей, но с атомарными операциями:
     * {@code compareAndSet} и {@code getAndAdd}, которых у {@code Field} нет.
     * @param name Имя
     * @param type Тип
     * @return {@link VarHandle} — то же для полей, но с атомарными операциями: {@code compareAndSet} и {@code getAndAdd}, которых у {@code Field} нет
     */
    public VarHandle field(final String name, final Class<?> type) throws ReflectiveOperationException {
        return MethodHandles.privateLookupIn(this.owner, MethodHandles.lookup())
            .findVarHandle(this.owner, name, type);
    }
}

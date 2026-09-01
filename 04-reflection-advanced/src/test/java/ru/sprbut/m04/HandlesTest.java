/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 34: MethodHandles и VarHandle.
 * @since 1.0
 */
@DisplayName("Слайд 34: MethodHandles и VarHandle")
final class HandlesTest {

    @Test
    @DisplayName("хэндл публичного метода вызывается напрямую")
    void invokesVirtualHandle() throws Throwable {
        final MethodHandle handle = new Handles(Counter.class).virtual("increment", int.class, int.class);
        MatcherAssert.assertThat(
            "virtual handle cannot invoke the method",
            (int) handle.invokeExact(new Counter(), 5),
            Matchers.equalTo(5)
        );
    }

    @Test
    @DisplayName("приватный метод доступен через privateLookupIn — аналог setAccessible")
    void invokesPrivateHandle() throws Throwable {
        final MethodHandle handle = new Handles(Counter.class)
            .hidden("describe", String.class, String.class);
        MatcherAssert.assertThat(
            "private lookup cannot reach the hidden method",
            (String) handle.invokeExact(new Counter(), "тест"),
            Matchers.equalTo("тест: счётчик=0")
        );
    }

    @Test
    @DisplayName("конструктор внутри зовётся <init> и возвращает void")
    void invokesConstructorHandle() throws Throwable {
        MatcherAssert.assertThat(
            "constructor handle cannot create the object",
            new Handles(Counter.class).constructor().invoke(),
            Matchers.instanceOf(Counter.class)
        );
    }

    @Test
    @DisplayName("VarHandle читает поле")
    void readsFieldHandle() throws Throwable {
        final VarHandle handle = new Handles(Counter.class).field("label", String.class);
        MatcherAssert.assertThat(
            "var handle cannot read the field",
            handle.get(new Counter()),
            Matchers.equalTo("счётчик")
        );
    }

    @Test
    @DisplayName("VarHandle умеет атомарные операции, которых у Field нет")
    void appliesAtomicOperation() throws Throwable {
        final Counter counter = new Counter();
        final VarHandle handle = new Handles(Counter.class).field("value", int.class);
        handle.getAndAdd(counter, 7);
        MatcherAssert.assertThat(
            "var handle cannot apply an atomic operation",
            counter.value(),
            Matchers.equalTo(7)
        );
    }

    @Test
    @DisplayName("compareAndSet меняет значение только от ожидаемого")
    void appliesCompareAndSet() throws Throwable {
        final Counter counter = new Counter();
        final VarHandle handle = new Handles(Counter.class).field("value", int.class);
        handle.compareAndSet(counter, 0, 3);
        MatcherAssert.assertThat(
            "compare and set cannot swap the expected value",
            counter.value(),
            Matchers.equalTo(3)
        );
    }

    @Test
    @DisplayName("хэндл связывается с получателем и становится композируемым")
    void bindsReceiver() throws Throwable {
        final MethodHandle bound = new Handles(Counter.class)
            .virtual("increment", int.class, int.class)
            .bindTo(new Counter());
        MatcherAssert.assertThat(
            "handle cannot be bound to its receiver",
            (int) bound.invoke(4),
            Matchers.equalTo(4)
        );
    }
}

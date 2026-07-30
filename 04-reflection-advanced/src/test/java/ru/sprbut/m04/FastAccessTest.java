package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 34: MethodHandles и VarHandle вместо Method и Field")
class FastAccessTest {

    @Test
    @DisplayName("Хэндл публичного метода вызывается напрямую, без Object[]")
    void invokesVirtualMethod() throws Throwable {
        MethodHandle handle = FastAccess.virtualMethod(
                FastAccess.Counter.class, "increment", int.class, int.class);

        FastAccess.Counter counter = new FastAccess.Counter();

        assertThat((int) handle.invokeExact(counter, 5)).isEqualTo(5);
        assertThat((int) handle.invokeExact(counter, 3)).isEqualTo(8);
    }

    @Test
    @DisplayName("invokeExact проверяет сигнатуру строго — это не Method.invoke")
    void invokeExactIsStrictlyTyped() throws Throwable {
        MethodHandle handle = FastAccess.virtualMethod(
                FastAccess.Counter.class, "increment", int.class, int.class);
        FastAccess.Counter counter = new FastAccess.Counter();

        // ожидается (Counter,int)int; попытка получить long — сразу ошибка типа
        assertThatThrownBy(() -> {
            long ignored = (long) handle.invokeExact(counter, 1);
        }).isInstanceOf(WrongMethodTypeException.class);

        // invoke() мягче: он сам приведёт типы
        assertThat((Object) handle.invoke(counter, 1)).isEqualTo(1);
    }

    @Test
    @DisplayName("privateLookupIn — аналог setAccessible(true) в мире хэндлов")
    void reachesPrivateMethod() throws Throwable {
        MethodHandle handle = FastAccess.privateMethod(
                FastAccess.Counter.class, "describe", String.class, String.class);
        FastAccess.Counter counter = new FastAccess.Counter();
        counter.increment(7);

        assertThat((String) handle.invokeExact(counter, "итог"))
                .isEqualTo("итог: счётчик=7");
    }

    @Test
    @DisplayName("Конструктор — это хэндл с именем <init> и возвращаемым типом void")
    void invokesConstructor() throws Throwable {
        MethodHandle ctor = FastAccess.constructor(FastAccess.Counter.class);

        Object created = ctor.invoke();

        assertThat(created).isInstanceOf(FastAccess.Counter.class);
        assertThat(((FastAccess.Counter) created).getValue()).isZero();
    }

    @Test
    @DisplayName("VarHandle читает и пишет поле, включая private")
    void readsAndWritesField() throws Throwable {
        VarHandle label = FastAccess.fieldHandle(FastAccess.Counter.class, "label", String.class);
        FastAccess.Counter counter = new FastAccess.Counter();

        assertThat((String) label.get(counter)).isEqualTo("счётчик");
        label.set(counter, "переименован");
        assertThat(counter.getLabel()).isEqualTo("переименован");
    }

    @Test
    @DisplayName("VarHandle умеет атомарные операции — у Field такого нет вовсе")
    void supportsAtomicOperations() throws Throwable {
        VarHandle value = FastAccess.fieldHandle(FastAccess.Counter.class, "value", int.class);
        FastAccess.Counter counter = new FastAccess.Counter();

        assertThat((int) value.getAndAdd(counter, 10)).isZero();
        assertThat(counter.getValue()).isEqualTo(10);

        assertThat(value.compareAndSet(counter, 10, 42)).isTrue();
        assertThat(value.compareAndSet(counter, 10, 99)).as("ожидаемое значение уже другое").isFalse();
        assertThat(counter.getValue()).isEqualTo(42);
    }

    @Test
    @DisplayName("bindTo фиксирует получателя — хэндлы композируются, Method нет")
    void bindsReceiver() throws Throwable {
        MethodHandle handle = FastAccess.virtualMethod(
                FastAccess.Counter.class, "increment", int.class, int.class);
        FastAccess.Counter counter = new FastAccess.Counter();

        MethodHandle bound = FastAccess.boundTo(handle, counter);

        assertThat((int) bound.invokeExact(4)).isEqualTo(4);
        assertThat(bound.type().parameterCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Права lookup определяются местом вызова, а не аргументами")
    void lookupRightsDependOnCallSite() {
        // Из чужого класса приватный метод Counter недоступен...
        assertThatThrownBy(() -> java.lang.invoke.MethodHandles.lookup().findVirtual(
                FastAccess.Counter.class, "describe",
                java.lang.invoke.MethodType.methodType(String.class, String.class)))
                .isInstanceOf(IllegalAccessException.class);

        // ...а из FastAccess — доступен: Counter его вложенный класс, они в одном nest
        assertThat(FastAccess.class.isNestmateOf(FastAccess.Counter.class)).isTrue();
    }
}

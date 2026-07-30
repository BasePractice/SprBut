package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

@DisplayName("Слайд 34: MethodHandles и VarHandle")
final class HandlesTest {

    @Test
    @DisplayName("хэндл публичного метода вызывается напрямую")
    void invokesVirtualHandle() throws Throwable {
        MethodHandle handle = new Handles(Counter.class).virtual("increment", int.class, int.class);
        assertThat(
            "virtual handle cannot invoke the method",
            (int) handle.invokeExact(new Counter(), 5),
            equalTo(5)
        );
    }

    @Test
    @DisplayName("приватный метод доступен через privateLookupIn — аналог setAccessible")
    void invokesPrivateHandle() throws Throwable {
        MethodHandle handle = new Handles(Counter.class)
            .hidden("describe", String.class, String.class);
        assertThat(
            "private lookup cannot reach the hidden method",
            (String) handle.invokeExact(new Counter(), "тест"),
            equalTo("тест: счётчик=0")
        );
    }

    @Test
    @DisplayName("конструктор внутри зовётся <init> и возвращает void")
    void invokesConstructorHandle() throws Throwable {
        assertThat(
            "constructor handle cannot create the object",
            new Handles(Counter.class).constructor().invoke(),
            instanceOf(Counter.class)
        );
    }

    @Test
    @DisplayName("VarHandle читает поле")
    void readsFieldHandle() throws Throwable {
        VarHandle handle = new Handles(Counter.class).field("label", String.class);
        assertThat(
            "var handle cannot read the field",
            handle.get(new Counter()),
            equalTo("счётчик")
        );
    }

    @Test
    @DisplayName("VarHandle умеет атомарные операции, которых у Field нет")
    void appliesAtomicOperation() throws Throwable {
        Counter counter = new Counter();
        VarHandle handle = new Handles(Counter.class).field("value", int.class);
        handle.getAndAdd(counter, 7);
        assertThat(
            "var handle cannot apply an atomic operation",
            counter.value(),
            equalTo(7)
        );
    }

    @Test
    @DisplayName("compareAndSet меняет значение только от ожидаемого")
    void appliesCompareAndSet() throws Throwable {
        Counter counter = new Counter();
        VarHandle handle = new Handles(Counter.class).field("value", int.class);
        handle.compareAndSet(counter, 0, 3);
        assertThat(
            "compare and set cannot swap the expected value",
            counter.value(),
            equalTo(3)
        );
    }

    @Test
    @DisplayName("хэндл связывается с получателем и становится композируемым")
    void bindsReceiver() throws Throwable {
        MethodHandle bound = new Handles(Counter.class)
            .virtual("increment", int.class, int.class)
            .bindTo(new Counter());
        assertThat(
            "handle cannot be bound to its receiver",
            (int) bound.invoke(4),
            equalTo(4)
        );
    }
}

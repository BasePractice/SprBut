package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("СХЕМА 2 (слайд 36): вызов → Proxy → InvocationHandler → цель")
class DynamicProxyTest {

    @Test
    @DisplayName("Все вызовы проходят через один InvocationHandler")
    void everyCallGoesThroughTheHandler() {
        List<String> log = new ArrayList<>();
        DynamicProxy.Greeter proxy = DynamicProxy.logging(
                DynamicProxy.Greeter.class, new DynamicProxy.SimpleGreeter(), log);

        assertThat(proxy.greet("Мир")).isEqualTo("Привет, Мир");
        assertThat(proxy.length("abc")).isEqualTo(3);

        assertThat(log).containsExactly(
                "→ greet", "← greet = Привет, Мир",
                "→ length", "← length = 3");
    }

    @Test
    @DisplayName("Прокси — сгенерированный класс, реализующий интерфейс; целевой класс не менялся")
    void proxyIsAGeneratedClass() {
        DynamicProxy.Greeter target = new DynamicProxy.SimpleGreeter();
        DynamicProxy.Greeter proxy = DynamicProxy.logging(
                DynamicProxy.Greeter.class, target, new ArrayList<>());

        assertThat(DynamicProxy.isProxy(proxy)).isTrue();
        assertThat(DynamicProxy.isProxy(target)).isFalse();
        assertThat(proxy).isInstanceOf(DynamicProxy.Greeter.class);
        assertThat(proxy.getClass()).isNotEqualTo(DynamicProxy.SimpleGreeter.class);
        assertThat(DynamicProxy.handlerOf(proxy)).isNotNull();
    }

    @Test
    @DisplayName("Прокси может работать вообще без цели — так устроен Spring Data")
    void proxyCanExistWithoutTarget() {
        DynamicProxy.Greeter stub = DynamicProxy.stub(DynamicProxy.Greeter.class);

        assertThat(stub.greet("кто угодно")).isNull();
        assertThat(stub.length("abc")).isZero();
    }

    @Test
    @DisplayName("Исключение цели прокидывается наружу развёрнутым")
    void unwrapsTargetException() {
        List<String> log = new ArrayList<>();
        DynamicProxy.Greeter failing = DynamicProxy.logging(DynamicProxy.Greeter.class,
                new DynamicProxy.SimpleGreeter() {
                    @Override
                    public String greet(String name) {
                        throw new IllegalStateException("цель упала");
                    }
                }, log);

        assertThatThrownBy(() -> failing.greet("Мир"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("цель упала");
        assertThat(log).anyMatch(e -> e.startsWith("✗ greet"));
    }

    @Test
    @DisplayName("Один прокси может реализовать несколько интерфейсов сразу")
    void supportsMultipleInterfaces() {
        Object proxy = DynamicProxy.multiInterface(
                new Class<?>[]{DynamicProxy.Greeter.class, Runnable.class},
                (p, method, args) -> "run".equals(method.getName()) ? null : "заглушка");

        assertThat(proxy).isInstanceOf(DynamicProxy.Greeter.class).isInstanceOf(Runnable.class);
        assertThat(((DynamicProxy.Greeter) proxy).greet("x")).isEqualTo("заглушка");
        ((Runnable) proxy).run();
    }

    @Test
    @DisplayName("Self-invocation минует прокси — корень будущей проблемы с @Transactional")
    void selfInvocationBypassesProxy() {
        List<String> log = DynamicProxy.demonstrateSelfInvocation();

        // Внешний вызов перехвачен...
        assertThat(log).anyMatch(e -> e.startsWith("→ greetTwice"));
        // ...а внутренний greet() ушёл напрямую в цель и в журнал не попал
        assertThat(log).noneMatch(e -> e.startsWith("→ greet "));
        assertThat(log.stream().filter(e -> e.startsWith("→")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("JDK-прокси умеет только интерфейсы — класс проксировать нельзя")
    void cannotProxyAClass() {
        assertThatThrownBy(() -> java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{DynamicProxy.SimpleGreeter.class},
                (p, m, a) -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not an interface");
    }
}

package ru.sprbut.m04;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Слайд 35, СХЕМА 2: Proxy и InvocationHandler")
final class LoggingProxyTest {

    @Test
    @DisplayName("прокси прозрачно возвращает результат цели")
    void keepsTargetResult() {
        assertThat(
            "proxy cannot pass the target result through",
            new LoggingProxy<>(Greeter.class, new SimpleGreeter(), new ArrayList<>())
                .proxy().greet("Мир"),
            equalTo("Привет, Мир")
        );
    }

    @Test
    @DisplayName("каждый вызов проходит через единственный обработчик")
    void routesEveryCallThroughHandler() {
        List<String> log = new ArrayList<>();
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greet("Мир");
        assertThat(
            "every call cannot reach the single handler",
            log,
            contains("→ greet", "← greet = Привет, Мир")
        );
    }

    @Test
    @DisplayName("self-invocation минует прокси — основа поведения @Transactional")
    void dontInterceptSelfInvocation() {
        List<String> log = new ArrayList<>();
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greetTwice("Мир");
        assertThat(
            "inner call cannot bypass the proxy",
            log,
            not(hasItem("→ greet"))
        );
    }

    @Test
    @DisplayName("сам default-метод через прокси проходит")
    void interceptsDefaultMethod() {
        List<String> log = new ArrayList<>();
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greetTwice("Мир");
        assertThat(
            "default method cannot be intercepted",
            log,
            hasItem("→ greetTwice")
        );
    }

    @Test
    @DisplayName("прокси без цели синтезирует ответ из метаданных — так работает Spring Data")
    void synthesizesAnswerWithoutTarget() {
        assertThat(
            "target free proxy cannot answer from metadata alone",
            new StubProxy<>(Greeter.class).proxy().greet("Мир"),
            nullValue()
        );
    }

    @Test
    @DisplayName("для примитивного типа заглушка возвращает его ноль, а не null")
    void answersPrimitiveZero() {
        assertThat(
            "primitive return cannot default to its own zero",
            new StubProxy<>(Greeter.class).proxy().length("текст"),
            equalTo(0)
        );
    }

    @Test
    @DisplayName("сгенерированный класс опознаётся как прокси")
    void detectsGeneratedProxy() {
        assertThat(
            "generated class cannot be recognised as a proxy",
            new ProxyFacts(new StubProxy<>(Greeter.class).proxy()).generated(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("обычный объект прокси не является")
    void dontCallPlainObjectProxy() {
        assertThat(
            "plain object cannot avoid the proxy verdict",
            new ProxyFacts(new SimpleGreeter()).generated(),
            equalTo(false)
        );
    }
}

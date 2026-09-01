/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 35, СХЕМА 2: Proxy и InvocationHandler.
 * @since 1.0
 */
@DisplayName("Слайд 35, СХЕМА 2: Proxy и InvocationHandler")
final class LoggingProxyTest {

    @Test
    @DisplayName("прокси прозрачно возвращает результат цели")
    void keepsTargetResult() {
        MatcherAssert.assertThat(
            "proxy cannot pass the target result through",
            new LoggingProxy<>(Greeter.class, new SimpleGreeter(), new ArrayList<>(0))
                .proxy().greet("Мир"),
            Matchers.equalTo("Привет, Мир")
        );
    }

    @Test
    @DisplayName("каждый вызов проходит через единственный обработчик")
    void routesEveryCallThroughHandler() {
        final List<String> log = new ArrayList<>(0);
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greet("Мир");
        MatcherAssert.assertThat(
            "every call cannot reach the single handler",
            log,
            Matchers.contains("→ greet", "← greet = Привет, Мир")
        );
    }

    @Test
    @DisplayName("self-invocation минует прокси — основа поведения @Transactional")
    void dontInterceptSelfInvocation() {
        final List<String> log = new ArrayList<>(0);
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greetTwice("Мир");
        MatcherAssert.assertThat(
            "inner call cannot bypass the proxy",
            log,
            Matchers.not(Matchers.hasItem("→ greet"))
        );
    }

    @Test
    @DisplayName("сам default-метод через прокси проходит")
    void interceptsDefaultMethod() {
        final List<String> log = new ArrayList<>(0);
        new LoggingProxy<>(Greeter.class, new SimpleGreeter(), log).proxy().greetTwice("Мир");
        MatcherAssert.assertThat(
            "default method cannot be intercepted",
            log,
            Matchers.hasItem("→ greetTwice")
        );
    }

    @Test
    @DisplayName("прокси без цели синтезирует ответ из метаданных — так работает Spring Data")
    void synthesizesAnswerWithoutTarget() {
        MatcherAssert.assertThat(
            "target free proxy cannot answer from metadata alone",
            new StubProxy<>(Greeter.class).proxy().greet("Мир"),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("для примитивного типа заглушка возвращает его ноль, а не null")
    void answersPrimitiveZero() {
        MatcherAssert.assertThat(
            "primitive return cannot default to its own zero",
            new StubProxy<>(Greeter.class).proxy().length("текст"),
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("сгенерированный класс опознаётся как прокси")
    void detectsGeneratedProxy() {
        MatcherAssert.assertThat(
            "generated class cannot be recognised as a proxy",
            new ProxyFacts(new StubProxy<>(Greeter.class).proxy()).generated(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("обычный объект прокси не является")
    void dontCallPlainObjectProxy() {
        MatcherAssert.assertThat(
            "plain object cannot avoid the proxy verdict",
            new ProxyFacts(new SimpleGreeter()).generated(),
            Matchers.equalTo(false)
        );
    }
}

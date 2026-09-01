/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Расширенный пример: мини-AOP на голом JDK.
 * @since 1.0
 */
@DisplayName("Расширенный пример: мини-AOP на голом JDK")
final class AspectedTest {

    @Test
    @DisplayName("@Cached: второй вызов с теми же аргументами цель не трогает")
    void cachesByArguments() {
        final RealPriceService real = new RealPriceService();
        final PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABC");
        MatcherAssert.assertThat(
            "cached call cannot skip the target on the second invocation",
            real.calls(),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("@Cached различает аргументы — другой ключ, другой вызов")
    void separatesCacheKeys() {
        final RealPriceService real = new RealPriceService();
        final PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABCD");
        MatcherAssert.assertThat(
            "different arguments cannot lead to different cache keys",
            real.calls(),
            Matchers.equalTo(2)
        );
    }

    @Test
    @DisplayName("@Timed пишет длительность вызова")
    void measuresDuration() {
        final Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().flaky();
        MatcherAssert.assertThat(
            "timed aspect cannot record the duration",
            journal.count("timed"),
            Matchers.equalTo(1L)
        );
    }

    @Test
    @DisplayName("@Retry повторяет вызов до успеха")
    void retriesUntilSuccess() {
        MatcherAssert.assertThat(
            "retry aspect cannot reach the successful attempt",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().flaky(),
            Matchers.equalTo(42)
        );
    }

    @Test
    @DisplayName("журнал фиксирует каждую неудачную попытку")
    void recordsFailedAttempts() {
        final Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().flaky();
        MatcherAssert.assertThat(
            "retry aspect cannot record the failed attempts",
            journal.count("retry-fail"),
            Matchers.greaterThanOrEqualTo(1L)
        );
    }

    @Test
    @DisplayName("@Stubbed подменяет результат — цель не вызывается вообще")
    void replacesTargetEntirely() {
        MatcherAssert.assertThat(
            "stub aspect cannot replace the target completely",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().currency(),
            Matchers.equalTo("RUB")
        );
    }

    @Test
    @DisplayName("метод без аннотаций проходит насквозь")
    void passesUnannotatedMethodThrough() {
        MatcherAssert.assertThat(
            "unannotated method cannot pass through untouched",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().plain(21),
            Matchers.equalTo(42)
        );
    }

    @Test
    @DisplayName("метод без аннотаций не оставляет следов в журнале")
    void dontLogUnannotatedMethod() {
        final Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().plain(1);
        MatcherAssert.assertThat(
            "unannotated method cannot leave the journal empty",
            journal.entries().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("self-invocation минует прокси — внутренние вызовы не кэшируются")
    void dontInterceptSelfInvocation() {
        final RealPriceService real = new RealPriceService();
        final PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.priceTwice("ABC");
        MatcherAssert.assertThat(
            "self invocation cannot bypass the caching aspect",
            real.calls(),
            Matchers.equalTo(2)
        );
    }

    @Test
    @DisplayName("внешний вызов того же метода аспект перехватывает нормально")
    void interceptsExternalCall() {
        final RealPriceService real = new RealPriceService();
        final PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABC");
        proxy.price("ABC");
        MatcherAssert.assertThat(
            "external calls cannot be intercepted by the caching aspect",
            real.calls(),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("проксировать класс нельзя — только интерфейс")
    void dontProxyClass() {
        MatcherAssert.assertThat(
            "class target cannot be rejected with an explanation",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Aspected<>(
                    RealPriceService.class, new RealPriceService(), new Journal()
                ).proxy()
            ).getMessage(),
            Matchers.containsString("только интерфейсы")
        );
    }

    @Test
    @DisplayName("toString не перехватывается — иначе прокси стал бы неотлаживаемым")
    void dontInterceptObjectMethods() {
        final Journal journal = new Journal();
        MatcherAssert.assertThat(
            String.format(
                "Object methods cannot stay out of the journal, proxy said %s",
                new Aspected<>(PriceService.class, new RealPriceService(), journal)
                    .proxy()
                    .toString()
            ),
            journal.entries().size(),
            Matchers.equalTo(0)
        );
    }
}

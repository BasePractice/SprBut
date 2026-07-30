package ru.sprbut.m04.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: мини-AOP на голом JDK")
final class AspectedTest {

    @Test
    @DisplayName("@Cached: второй вызов с теми же аргументами цель не трогает")
    void cachesByArguments() {
        RealPriceService real = new RealPriceService();
        PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABC");
        assertThat(
            "cached call cannot skip the target on the second invocation",
            real.calls(),
            equalTo(1)
        );
    }

    @Test
    @DisplayName("@Cached различает аргументы — другой ключ, другой вызов")
    void separatesCacheKeys() {
        RealPriceService real = new RealPriceService();
        PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABCD");
        assertThat(
            "different arguments cannot lead to different cache keys",
            real.calls(),
            equalTo(2)
        );
    }

    @Test
    @DisplayName("@Timed пишет длительность вызова")
    void measuresDuration() {
        Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().flaky();
        assertThat(
            "timed aspect cannot record the duration",
            journal.count("timed"),
            equalTo(1L)
        );
    }

    @Test
    @DisplayName("@Retry повторяет вызов до успеха")
    void retriesUntilSuccess() {
        assertThat(
            "retry aspect cannot reach the successful attempt",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().flaky(),
            equalTo(42)
        );
    }

    @Test
    @DisplayName("журнал фиксирует каждую неудачную попытку")
    void recordsFailedAttempts() {
        Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().flaky();
        assertThat(
            "retry aspect cannot record the failed attempts",
            journal.count("retry-fail"),
            greaterThanOrEqualTo(1L)
        );
    }

    @Test
    @DisplayName("@Stubbed подменяет результат — цель не вызывается вообще")
    void replacesTargetEntirely() {
        assertThat(
            "stub aspect cannot replace the target completely",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().currency(),
            equalTo("RUB")
        );
    }

    @Test
    @DisplayName("метод без аннотаций проходит насквозь")
    void passesUnannotatedMethodThrough() {
        assertThat(
            "unannotated method cannot pass through untouched",
            new Aspected<>(PriceService.class, new RealPriceService(), new Journal())
                .proxy().plain(21),
            equalTo(42)
        );
    }

    @Test
    @DisplayName("метод без аннотаций не оставляет следов в журнале")
    void dontLogUnannotatedMethod() {
        Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().plain(1);
        assertThat(
            "unannotated method cannot leave the journal empty",
            journal.entries().size(),
            equalTo(0)
        );
    }

    @Test
    @DisplayName("self-invocation минует прокси — внутренние вызовы не кэшируются")
    void dontInterceptSelfInvocation() {
        RealPriceService real = new RealPriceService();
        PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.priceTwice("ABC");
        assertThat(
            "self invocation cannot bypass the caching aspect",
            real.calls(),
            equalTo(2)
        );
    }

    @Test
    @DisplayName("внешний вызов того же метода аспект перехватывает нормально")
    void interceptsExternalCall() {
        RealPriceService real = new RealPriceService();
        PriceService proxy = new Aspected<>(PriceService.class, real, new Journal()).proxy();
        proxy.price("ABC");
        proxy.price("ABC");
        proxy.price("ABC");
        assertThat(
            "external calls cannot be intercepted by the caching aspect",
            real.calls(),
            equalTo(1)
        );
    }

    @Test
    @DisplayName("проксировать класс нельзя — только интерфейс")
    void dontProxyClass() {
        assertThat(
            "class target cannot be rejected with an explanation",
            assertThrows(
                IllegalArgumentException.class,
                () -> new Aspected<>(
                    RealPriceService.class, new RealPriceService(), new Journal()
                ).proxy()
            ).getMessage(),
            containsString("только интерфейсы")
        );
    }

    @Test
    @DisplayName("toString не перехватывается — иначе прокси стал бы неотлаживаемым")
    void dontInterceptObjectMethods() {
        Journal journal = new Journal();
        new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy().toString();
        assertThat(
            "Object methods cannot stay out of the journal",
            journal.entries().size(),
            equalTo(0)
        );
    }
}

package ru.sprbut.m04.extended;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Расширенный пример: мини-AOP на голом JDK")
class JdkAopFactoryTest {

    public interface PriceService {
        int compute(String sku);

        int flaky(String sku);

        String label();

        String heavy(int input);

        int selfCalling(String sku);
    }

    /** Реальная цель: считает вызовы, чтобы было видно, сработал ли аспект. */
    public static class RealPriceService implements PriceService {

        final AtomicInteger computeCalls = new AtomicInteger();
        final AtomicInteger flakyCalls = new AtomicInteger();
        int failuresBeforeSuccess;

        @Override
        @Aspects.Cached
        @Aspects.Timed
        public int compute(String sku) {
            computeCalls.incrementAndGet();
            return sku.length() * 10;
        }

        @Override
        @Aspects.Retry(attempts = 3)
        public int flaky(String sku) {
            int call = flakyCalls.incrementAndGet();
            if (call <= failuresBeforeSuccess) {
                throw new IllegalStateException("временный сбой №" + call);
            }
            return 42;
        }

        @Override
        @Aspects.Stubbed("заглушка")
        public String label() {
            throw new AssertionError("цель не должна вызываться вовсе");
        }

        @Override
        public String heavy(int input) {
            return "тяжёлый-" + input;
        }

        @Override
        public int selfCalling(String sku) {
            // внутренний вызов идёт напрямую, минуя прокси
            return compute(sku) + compute(sku);
        }
    }

    private RealPriceService target;
    private JdkAopFactory.Journal journal;
    private PriceService proxy;

    @BeforeEach
    void setUp() {
        target = new RealPriceService();
        journal = new JdkAopFactory.Journal();
        proxy = JdkAopFactory.wrap(PriceService.class, target, journal);
    }

    @Nested
    @DisplayName("Аспекты применяются по аннотациям в runtime")
    class Aspecting {

        @Test
        @DisplayName("@Cached: второй вызов с теми же аргументами цель не трогает")
        void cachesByArguments() {
            assertThat(proxy.compute("ABC")).isEqualTo(30);
            assertThat(proxy.compute("ABC")).isEqualTo(30);

            assertThat(target.computeCalls.get()).isEqualTo(1);
            assertThat(journal.count("cache-miss")).isEqualTo(1);
            assertThat(journal.count("cache-hit")).isEqualTo(1);
        }

        @Test
        @DisplayName("@Cached различает аргументы — другой ключ, другой вызов")
        void cacheKeyIncludesArguments() {
            proxy.compute("ABC");
            proxy.compute("ABCD");

            assertThat(target.computeCalls.get()).isEqualTo(2);
            assertThat(journal.count("cache-hit")).isZero();
        }

        @Test
        @DisplayName("@Timed пишет длительность каждого вызова")
        void measuresDuration() {
            proxy.compute("ABC");

            assertThat(journal.entries())
                    .anyMatch(e -> e.startsWith("timed compute") && e.endsWith("ns"));
        }

        @Test
        @DisplayName("@Retry повторяет вызов до успеха")
        void retriesUntilSuccess() {
            target.failuresBeforeSuccess = 2;

            assertThat(proxy.flaky("X")).isEqualTo(42);

            assertThat(target.flakyCalls.get()).isEqualTo(3);
            assertThat(journal.count("retry-fail")).isEqualTo(2);
            assertThat(journal.entries()).contains("retry-success flaky попытка 3");
        }

        @Test
        @DisplayName("@Retry исчерпывает попытки и пробрасывает последнее исключение")
        void rethrowsAfterExhaustingAttempts() {
            target.failuresBeforeSuccess = 99;

            assertThatThrownBy(() -> proxy.flaky("X"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("временный сбой №3");

            assertThat(target.flakyCalls.get()).isEqualTo(3);
            assertThat(journal.entries()).contains("retry-exhausted flaky");
        }

        @Test
        @DisplayName("@Stubbed подменяет результат — цель не вызывается вообще")
        void stubReplacesTargetEntirely() {
            assertThat(proxy.label()).isEqualTo("заглушка");
            assertThat(journal.entries()).contains("stub label");
        }

        @Test
        @DisplayName("Метод без аннотаций проходит насквозь без побочных эффектов")
        void unannotatedMethodPassesThrough() {
            assertThat(proxy.heavy(7)).isEqualTo("тяжёлый-7");
            assertThat(journal.entries()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Ограничения, объясняющие поведение Spring AOP")
    class Limits {

        @Test
        @DisplayName("Self-invocation минует прокси — @Cached внутри объекта не работает")
        void selfInvocationBypassesAspects() {
            assertThat(proxy.selfCalling("ABC")).isEqualTo(60);

            // оба внутренних вызова дошли до цели: кэш не сработал ни разу
            assertThat(target.computeCalls.get()).isEqualTo(2);
            assertThat(journal.count("cache-hit")).isZero();
            assertThat(journal.count("cache-miss")).isZero();
        }

        @Test
        @DisplayName("Внешний вызов того же метода аспект перехватывает нормально")
        void externalCallIsIntercepted() {
            proxy.compute("ABC");
            proxy.compute("ABC");

            assertThat(target.computeCalls.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Проксировать класс нельзя — только интерфейс")
        void requiresAnInterface() {
            assertThatThrownBy(() -> JdkAopFactory.wrap(
                    RealPriceService.class, new RealPriceService(), journal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не интерфейс");
        }

        @Test
        @DisplayName("equals/hashCode/toString не проксируются — иначе прокси станет неюзабельным")
        void objectMethodsAreNotIntercepted() {
            assertThat(proxy.toString()).isEqualTo(target.toString());
            assertThat(proxy.hashCode()).isEqualTo(target.hashCode());
            assertThat(journal.entries()).isEmpty();
        }
    }
}

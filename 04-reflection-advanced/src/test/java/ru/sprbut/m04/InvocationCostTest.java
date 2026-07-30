package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 33: рефлексия медленнее прямого вызова")
class InvocationCostTest {

    private static final int ITERATIONS = 20_000;

    @Test
    @DisplayName("Все четыре способа дают одинаковый результат — различается только цена")
    void allStrategiesAgree() throws Throwable {
        InvocationCost.Target target = new InvocationCost.Target();

        assertThat(InvocationCost.direct(target, ITERATIONS)).isEqualTo(ITERATIONS);
        assertThat(InvocationCost.reflectionCached(target, ITERATIONS)).isEqualTo(ITERATIONS);
        assertThat(InvocationCost.reflectionWithLookup(target, ITERATIONS)).isEqualTo(ITERATIONS);
        assertThat(InvocationCost.methodHandle(target, ITERATIONS)).isEqualTo(ITERATIONS);
    }

    @Test
    @DisplayName("Замер отдаёт время всех четырёх стратегий")
    void benchmarkReportsAllStrategies() throws Throwable {
        Map<String, Long> timings = InvocationCost.benchmark(ITERATIONS);

        assertThat(timings).containsOnlyKeys(
                "direct", "methodHandle", "reflectionCached", "reflectionWithLookup");
        assertThat(timings.values()).allSatisfy(nanos -> assertThat(nanos).isPositive());
    }

    @Test
    @DisplayName("Поиск метода на каждой итерации дороже закэшированного — воспроизводимо без JMH")
    void lookupInLoopIsTheWorstOption() throws Throwable {
        // Разогрев, чтобы JIT успел скомпилировать оба варианта
        InvocationCost.benchmark(ITERATIONS);

        Map<String, Long> timings = InvocationCost.benchmark(ITERATIONS);

        // Единственное утверждение о скорости, устойчивое на любой машине:
        // getDeclaredMethod в цикле заведомо дороже, чем закэшированный Method.
        assertThat(timings.get("reflectionWithLookup"))
                .as("поиск метода в цикле должен быть дороже закэшированного")
                .isGreaterThan(timings.get("reflectionCached"));
    }
}

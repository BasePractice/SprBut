package ru.sprbut.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 33: рефлексия медленнее прямого вызова")
final class InvocationCostTest {

    @Test
    @DisplayName("прямой вызов даёт эталонный результат")
    void computesDirectly() {
        assertThat(
            "direct invocation cannot produce the reference result",
            new InvocationCost(new Target(), 1000).direct(),
            equalTo(1000)
        );
    }

    @Test
    @DisplayName("поиск метода на каждой итерации даёт тот же результат — но дороже всех")
    void computesWithLookupEachTime() throws ReflectiveOperationException {
        assertThat(
            "per-call lookup cannot produce the same result",
            new InvocationCost(new Target(), 1000).searching(),
            equalTo(1000)
        );
    }

    @Test
    @DisplayName("кэшированный Method даёт тот же результат")
    void computesWithCachedMethod() throws ReflectiveOperationException {
        assertThat(
            "cached method cannot produce the same result",
            new InvocationCost(new Target(), 1000).cached(),
            equalTo(1000)
        );
    }

    @Test
    @DisplayName("MethodHandle даёт тот же результат — разница только в цене")
    void computesWithHandle() throws Throwable {
        assertThat(
            "method handle cannot produce the same result",
            new InvocationCost(new Target(), 1000).handle(),
            equalTo(1000)
        );
    }
}

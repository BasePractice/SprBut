/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 33: рефлексия медленнее прямого вызова.
 * @since 1.0
 */
@DisplayName("Слайд 33: рефлексия медленнее прямого вызова")
final class InvocationCostTest {

    @Test
    @DisplayName("прямой вызов даёт эталонный результат")
    void computesDirectly() {
        MatcherAssert.assertThat(
            "direct invocation cannot produce the reference result",
            new InvocationCost(new Target(), 1000).direct(),
            Matchers.equalTo(1000)
        );
    }

    @Test
    @DisplayName("поиск метода на каждой итерации даёт тот же результат — но дороже всех")
    void computesWithLookupEachTime() throws ReflectiveOperationException {
        MatcherAssert.assertThat(
            "per-call lookup cannot produce the same result",
            new InvocationCost(new Target(), 1000).searching(),
            Matchers.equalTo(1000)
        );
    }

    @Test
    @DisplayName("кэшированный Method даёт тот же результат")
    void computesWithCachedMethod() throws ReflectiveOperationException {
        MatcherAssert.assertThat(
            "cached method cannot produce the same result",
            new InvocationCost(new Target(), 1000).cached(),
            Matchers.equalTo(1000)
        );
    }

    @Test
    @DisplayName("MethodHandle даёт тот же результат — разница только в цене")
    void computesWithHandle() throws Throwable {
        MatcherAssert.assertThat(
            "method handle cannot produce the same result",
            new InvocationCost(new Target(), 1000).handle(),
            Matchers.equalTo(1000)
        );
    }
}

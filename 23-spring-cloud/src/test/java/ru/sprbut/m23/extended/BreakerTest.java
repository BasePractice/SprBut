/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Расширенный пример: отказ соседа как нормальный режим работы.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Расширенный пример: отказ соседа как нормальный режим работы")
final class BreakerTest {

    /**
     * Предохранитель.
     */
    @Autowired
    private Breaker breaker;

    @Test
    @DisplayName("вместо исключения вызывающий получает запасной ответ")
    void answersWithFallback() {
        MatcherAssert.assertThat(
            "молчание соседа дошло до вызывающего исключением",
            this.breaker.fallback(),
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("после череды отказов вызовы перестают доходить до соседа")
    void stopsCallingBrokenNeighbour() {
        MatcherAssert.assertThat(
            "предохранитель не разомкнул цепь: до соседа дошли все вызовы",
            this.breaker.attempts(20),
            Matchers.lessThan(20)
        );
    }

    @Test
    @DisplayName("первые вызовы всё же уходят: предохранитель судит по опыту")
    void triesBeforeGivingUp() {
        MatcherAssert.assertThat(
            "предохранитель разомкнул цепь, не сделав ни одной попытки",
            this.breaker.attempts(20),
            Matchers.greaterThan(0)
        );
    }
}

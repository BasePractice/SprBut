/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.extended;

import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.sprbut.m21.domain.Quotes;

/**
 * Расширенный пример: потребитель задаёт темп.
 * @since 1.0
 */
@DisplayName("Расширенный пример: потребитель задаёт темп")
final class DemandTest {

    @Test
    @DisplayName("подписчик получает ровно столько элементов, сколько запросил")
    void takesExactlyWhatWasRequested() {
        MatcherAssert.assertThat(
            "источник отдал больше, чем просил подписчик",
            new Demand().taken(Flux.range(1, 1000).map(Long::valueOf), 3L),
            Matchers.contains(1L, 2L, 3L)
        );
    }

    @Test
    @DisplayName("источник не производит ничего сверх запрошенного")
    void producesNothingExtra() {
        MatcherAssert.assertThat(
            "ленивый источник посчитал лишние элементы",
            new Demand().produced(5L),
            Matchers.equalTo(5L)
        );
    }

    @Test
    @DisplayName("нулевой спрос не порождает ни одного элемента")
    void producesNothingWithoutDemand() {
        MatcherAssert.assertThat(
            "без спроса источник всё равно что-то произвёл",
            new Demand().produced(0L),
            Matchers.equalTo(0L)
        );
    }

    @Test
    @DisplayName("виртуальное время доказывает, что задержка не занимает поток")
    void delaysWithoutBlocking() {
        StepVerifier.withVirtualTime(() -> new Quotes().paced(Duration.ofHours(1)))
            .thenAwait(Duration.ofHours(3))
            .expectNextCount(3L)
            .verifyComplete();
    }
}

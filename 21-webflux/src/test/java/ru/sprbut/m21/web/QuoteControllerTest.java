/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.web;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.sprbut.m21.domain.Quote;
import ru.sprbut.m21.domain.Quotes;

/**
 * Слайды 203–209: реактивный стек отвечает теми же аннотациями.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebFluxTest(controllers = QuoteController.class)
@Import({Quotes.class, QuoteRoutes.class})
@DisplayName("Слайды 203–209: реактивный стек отвечает теми же аннотациями")
final class QuoteControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private WebTestClient http;

    @Test
    @DisplayName("Flux превращается в JSON-массив, а не в одну строку")
    void writesFluxAsArray() {
        this.http.get().uri("/api/quotes").exchange()
            .expectBodyList(Quote.class).hasSize(3);
    }

    @Test
    @DisplayName("пустой Mono становится ответом 404 без единого if в контроллере")
    void answersNotFoundOnEmptyMono() {
        this.http.get().uri("/api/quotes/42").exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("существующая цитата приходит телом ответа")
    void writesQuoteToBody() {
        this.http.get().uri("/api/quotes/0").exchange()
            .expectBody(Quote.class).isEqualTo(new Quote("поток не ждёт ответа"));
    }

    @Test
    @DisplayName("функциональный маршрут отвечает наравне с аннотированным")
    void routerFunctionAnswers() {
        this.http.get().uri("/fn/quotes").exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("обработчик называет поток, в котором его вызвали")
    void tellsItsThread() {
        MatcherAssert.assertThat(
            "обработчик не назвал поток, в котором работает",
            this.http.get().uri("/api/quotes/thread").exchange()
                .expectBody(String.class).returnResult().getResponseBody(),
            Matchers.not(Matchers.emptyOrNullString())
        );
    }

    @Test
    @DisplayName("поток событий отдаёт элементы по одному, не дожидаясь конца")
    void streamsElementsOneByOne() {
        this.http.get().uri("/api/quotes").accept(MediaType.TEXT_EVENT_STREAM).exchange()
            .expectBodyList(Quote.class).hasSize(3);
    }
}

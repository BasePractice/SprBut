/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.sprbut.m21.domain.Quote;
import ru.sprbut.m21.domain.Quotes;

/**
 * Слайд 207: функциональные маршруты вместо аннотаций.
 *
 * <p>Тот же результат, что у {@link QuoteController}, но маршрут здесь —
 * обычный объект, а не метаданные. Его можно собрать в цикле, передать
 * в метод, склеить с другим: аннотация так не умеет.</p>
 *
 * <p>Разница глубже синтаксиса. Аннотации читает контейнер при старте
 * (модули 01–06), а {@code RouterFunction} — это код, который вы пишете сами
 * и который выполняется на каждый запрос.</p>
 *
 * @since 1.0
 */
@Configuration
public class QuoteRoutes {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public QuoteRoutes() {
        // нечего инициализировать
    }

    /**
     * Маршрут, собранный как объект.
     * @param quotes Цитаты
     * @return Маршрут, собранный как объект
     */
    @Bean
    public RouterFunction<ServerResponse> routes(final Quotes quotes) {
        return RouterFunctions.route(
            RequestPredicates.GET("/fn/quotes"),
            request -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(quotes.all(), Quote.class)
        );
    }
}

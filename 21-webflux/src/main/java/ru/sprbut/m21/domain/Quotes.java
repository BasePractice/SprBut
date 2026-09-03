/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.domain;

import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Слайды 204–205: {@code Mono} и {@code Flux} как источник данных.
 *
 * <p>Источник возвращает не значения, а <b>описание</b> будущих значений.
 * Пока никто не подписался, не выполняется ничего: {@code Flux} — это рецепт,
 * а не блюдо. Отсюда и главное отличие от привычного кода — метод завершается
 * мгновенно, задолго до того, как появятся данные.</p>
 *
 * @since 1.0
 */
@Component
public final class Quotes {

    /**
     * Цитаты, которые источник выдаёт по одной.
     */
    private final List<String> texts;

    /**
     * Основной конструктор.
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public Quotes() {
        this.texts = List.of(
            "поток не ждёт ответа",
            "потребитель задаёт темп",
            "рецепт — ещё не блюдо"
        );
    }

    /**
     * Слайд 204: {@code Mono} — ноль или один элемент.
     * @param index Номер цитаты
     * @return Цитата по номеру или пустой Mono
     */
    public Mono<Quote> one(final int index) {
        final Mono<Quote> found;
        if (index < 0 || index >= this.texts.size()) {
            found = Mono.empty();
        } else {
            found = Mono.just(new Quote(this.texts.get(index)));
        }
        return found;
    }

    /**
     * Слайд 204: {@code Flux} — поток из многих элементов.
     * @return Все цитаты
     */
    public Flux<Quote> all() {
        return Flux.fromIterable(this.texts).map(Quote::new);
    }

    /**
     * Слайд 205: элементы приходят с задержкой, но поток при этом не занят.
     *
     * <p>{@code delayElements} не усыпляет вызывающий поток: задержка живёт
     * на планировщике, а подписчик получает элементы по мере готовности.
     * В сервлетной модели такой же код держал бы поток контейнера занятым
     * всё это время.</p>
     *
     * @param pause Пауза между элементами
     * @return Цитаты, приходящие по одной
     */
    public Flux<Quote> paced(final Duration pause) {
        return this.all().delayElements(pause);
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.web;

import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sprbut.m21.domain.Quote;
import ru.sprbut.m21.domain.Quotes;

/**
 * Слайды 203–204: те же аннотации, другой тип возвращаемого значения.
 *
 * <p>Снаружи контроллер выглядит как в модуле 20 — те же
 * {@code @RestController} и {@code @GetMapping}. Разница в одном: метод
 * возвращает не данные, а {@code Mono} или {@code Flux}, то есть обещание
 * данных. Поэтому метод завершается сразу, а поток освобождается до того,
 * как ответ будет собран.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/quotes")
public final class QuoteController {

    /**
     * Цитаты.
     */
    private final Quotes quotes;

    /**
     * Основной конструктор.
     * @param quotes Цитаты
     */
    public QuoteController(final Quotes quotes) {
        this.quotes = quotes;
    }

    /**
     * Все цитаты одним ответом.
     *
     * <p>Элемент потока — {@link Quote}, а не строка, и это не косметика:
     * {@code Flux<String>} WebFlux пишет как поток символов при любом
     * {@code produces}, потому что {@code CharSequence} кодируется раньше
     * Jackson. Массивом JSON поток становится, только если его элемент —
     * объект.</p>
     *
     * @return Все цитаты
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Quote> all() {
        return this.quotes.all();
    }

    /**
     * Слайд 204: пустой {@code Mono} — это не ошибка, а отсутствие значения.
     *
     * <p>Само по себе пустое обещание даёт ответ 200 с пустым телом: поток
     * завершился, ничего не отдав, и это законный исход. Код 404 приходится
     * назвать явно — {@code defaultIfEmpty}. Разница между «пусто» и «нет
     * такого» остаётся решением приложения, а не свойством {@code Mono}.</p>
     *
     * @param index Номер цитаты
     * @return Цитата по номеру или ответ 404
     */
    @GetMapping("/{index}")
    public Mono<ResponseEntity<Quote>> one(@PathVariable final int index) {
        return this.quotes.one(index)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Слайд 205: поток событий, который отдаётся по мере готовности.
     *
     * <p>{@code text/event-stream} отдаёт элементы клиенту сразу, не дожидаясь
     * конца потока. В сервлетной модели тот же ответ держал бы поток
     * контейнера занятым всё время передачи.</p>
     *
     * @return Цитаты, приходящие по одной
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Quote> stream() {
        return this.quotes.paced(Duration.ofMillis(10));
    }

    /**
     * Слайд 209: имя потока, в котором работает обработчик.
     *
     * <p>На запущенном приложении тот же метод, что в модуле 20, отвечает
     * именем потока событийного цикла Netty — {@code reactor-http-nio-*}.
     * Потоков этих по числу ядер, и ни один из них не имеет права
     * блокироваться: занятый событийный цикл не обслуживает никого.</p>
     *
     * @return Имя потока, обрабатывающего запрос
     */
    @GetMapping("/thread")
    public Mono<String> thread() {
        return Mono.fromSupplier(() -> Thread.currentThread().getName());
    }
}

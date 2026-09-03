/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m21.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Component;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * <b>Расширенный пример модуля.</b>
 *
 * <p>Backpressure, показанный не на словах. Подписчик просит ровно столько
 * элементов, сколько готов принять, и источник отдаёт ровно столько — ни одним
 * больше, даже если элементов у него в тысячу раз больше.</p>
 *
 * <p>Это и есть ответ на вопрос «чем поток отличается от коллекции»: коллекцию
 * отдают целиком и сразу, поток — по запросу потребителя. Без такого договора
 * быстрый источник просто переполнил бы медленного получателя, и вся разница
 * между «медленно» и «упало» свелась бы к размеру буфера.</p>
 *
 * @since 1.0
 */
@Component
public final class Demand {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Demand() {
        // нечего инициализировать
    }

    /**
     * Сколько элементов дойдёт до подписчика, попросившего ограниченное число.
     * @param source Источник элементов
     * @param limit Сколько элементов запрашивает подписчик
     * @return Полученные элементы в порядке поступления
     */
    public List<Long> taken(final Flux<Long> source, final long limit) {
        final List<Long> taken = new ArrayList<>(0);
        source.subscribe(
            new BaseSubscriber<Long>() {
                @Override
                protected void hookOnSubscribe(final Subscription subscription) {
                    subscription.request(limit);
                }

                @Override
                protected void hookOnNext(final Long value) {
                    taken.add(value);
                }
            }
        );
        return List.copyOf(taken);
    }

    /**
     * Сколько элементов источник успел произвести под таким спросом.
     *
     * <p>Разница с {@link #taken(Flux, long)} в том, что здесь считается
     * работа <b>источника</b>, а не подписчика: ленивый источник не производит
     * ничего сверх запрошенного, и это видно по счётчику.</p>
     *
     * @param limit Сколько элементов запрашивает подписчик
     * @return Сколько элементов было произведено источником
     */
    public long produced(final long limit) {
        final AtomicLong count = new AtomicLong();
        this.taken(
            Flux.range(1, 1000).map(Long::valueOf).doOnNext(item -> count.incrementAndGet()),
            limit
        );
        return count.get();
    }
}

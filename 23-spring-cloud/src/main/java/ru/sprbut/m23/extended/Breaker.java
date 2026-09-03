/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.extended;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

/**
 * <b>Расширенный пример модуля.</b>
 *
 * <p>Предохранитель, пойманный за работой. Сосед падает на каждом вызове,
 * запросов делается двадцать — а до сети доходит лишь несколько первых.
 * Разница между числом запросов и числом попыток и есть та защита,
 * ради которой всё затевалось.</p>
 *
 * <p>Здесь видно, чем распределённая система отличается от одного
 * приложения. В монолите вызов метода либо выполняется, либо бросает
 * исключение; между сервисами появляется третий исход — «не отвечает», и он
 * не редкость, а нормальный режим. Предохранитель переводит этот исход
 * из аварии в ожидаемое поведение: сначала считает неудачи, потом перестаёт
 * тратить на соседа время, а через паузу пробует снова.</p>
 *
 * @since 1.0
 */
@Component
public final class Breaker {

    /**
     * Фабрика предохранителей.
     */
    private final CircuitBreakerFactory<?, ?> breakers;

    /**
     * Счётчик опытов: каждому нужен свой предохранитель, не помнящий чужих неудач.
     */
    private final AtomicInteger runs;

    /**
     * Основной конструктор.
     * @param breakers Фабрика предохранителей
     */
    public Breaker(final CircuitBreakerFactory<?, ?> breakers) {
        this.breakers = breakers;
        this.runs = new AtomicInteger();
    }

    /**
     * Сколько вызовов дошло до соседа, который падает всегда.
     * @param calls Сколько раз приложение обратилось за остатком
     * @return Сколько вызовов дошло до соседа
     */
    public int attempts(final int calls) {
        final AtomicInteger reached = new AtomicInteger();
        final String name = String.format("probe-%d", this.runs.incrementAndGet());
        for (int call = 0; call < calls; call = call + 1) {
            this.breakers.create(name).run(
                () -> {
                    reached.incrementAndGet();
                    throw new IllegalStateException("склад не отвечает");
                },
                failure -> 0
            );
        }
        return reached.get();
    }

    /**
     * Что получает вызывающий, пока сосед молчит.
     * @return Запасной ответ вместо исключения
     */
    public int fallback() {
        return this.breakers.create(String.format("probe-%d", this.runs.incrementAndGet())).run(
            () -> {
                throw new IllegalStateException("склад не отвечает");
            },
            failure -> 0
        );
    }
}

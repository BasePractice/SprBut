/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.extended;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.sprbut.m09.UserMapper;
import ru.sprbut.m09.model.UserEntity;

/**
 * Время работы каждой реализации на одинаковой нагрузке.
 *
 * <p>Это не JMH: цель — порядок величины, а не точные цифры. Разогрев обязателен,
 * иначе замер покажет скорость интерпретатора, а не скомпилированного кода,
 * и рефлексия окажется «быстрее» просто потому, что мерили не то.</p>
 *
 * <p>Тесты на этих числах ничего не утверждают: замеры на CI флаки по природе.</p>
 *
 * @since 1.0
 */
public final class Benchmark {

    /**
     * Мапперы.
     */
    private final Mappers mappers;

    /**
     * Сущность.
     */
    private final UserEntity entity;

    /**
     * Основной конструктор.
     * @param mappers Мапперы
     * @param entity Сущность
     */
    public Benchmark(final Mappers mappers, final UserEntity entity) {
        this.mappers = mappers;
        this.entity = entity;
    }

    /**
     * Время в наносекундах на каждую реализацию.
     * @param iterations Число итераций
     * @return Время в наносекундах на каждую реализацию
     */
    public Map<String, Long> timings(final int iterations) {
        final Map<String, Long> measured = new LinkedHashMap<>();
        for (UserMapper mapper : this.mappers.list()) {
            this.warmup(mapper, iterations / 10 + 1);
            final long started = System.nanoTime();
            for (int step = 0; step < iterations; step++) {
                mapper.toDto(this.entity);
            }
            measured.put(mapper.getClass().getSimpleName(), System.nanoTime() - started);
        }
        return Map.copyOf(measured);
    }

    private void warmup(final UserMapper mapper, final int rounds) {
        for (int step = 0; step < rounds; step++) {
            mapper.toDto(this.entity);
        }
    }
}

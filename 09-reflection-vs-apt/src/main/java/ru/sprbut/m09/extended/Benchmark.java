package ru.sprbut.m09.extended;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.sprbut.m09.UserMapper;
import ru.sprbut.m09.model.UserEntity;

/**
 * Время работы каждой реализации на одинаковой нагрузке.
 * <p>
 * Это не JMH: цель — порядок величины, а не точные цифры. Разогрев обязателен,
 * иначе замер покажет скорость интерпретатора, а не скомпилированного кода,
 * и рефлексия окажется «быстрее» просто потому, что мерили не то.
 * <p>
 * Тесты на этих числах ничего не утверждают: замеры на CI флаки по природе.
 */
public final class Benchmark {

    private final Mappers mappers;

    private final UserEntity entity;

    public Benchmark(Mappers mappers, UserEntity entity) {
        this.mappers = mappers;
        this.entity = entity;
    }

    /**
     * Время в наносекундах на каждую реализацию.
     */
    public Map<String, Long> timings(int iterations) {
        Map<String, Long> measured = new LinkedHashMap<>();
        for (UserMapper mapper : this.mappers.list()) {
            warmup(mapper, iterations / 10 + 1);
            long started = System.nanoTime();
            for (int step = 0; step < iterations; step++) {
                mapper.toDto(this.entity);
            }
            measured.put(mapper.getClass().getSimpleName(), System.nanoTime() - started);
        }
        return Map.copyOf(measured);
    }

    private void warmup(UserMapper mapper, int rounds) {
        for (int step = 0; step < rounds; step++) {
            mapper.toDto(this.entity);
        }
    }
}

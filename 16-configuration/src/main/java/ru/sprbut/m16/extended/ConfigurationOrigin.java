/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m16.extended;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * <b>Расширенный пример модуля 16.</b>
 *
 * <p>Инструмент, отвечающий на вопрос «откуда взялось это значение» — прямая
 * реализация СХЕМЫ 10 (слайд 138). Приоритеты со слайда перестают быть списком,
 * который надо помнить: {@code Environment} показывает весь стек источников
 * целиком и говорит, какой из них победил.</p>
 *
 * <p>Без такого инструмента вопрос «почему приложение слушает не тот порт»
 * решается перебором.</p>
 *
 * @since 1.0
 */
public final class ConfigurationOrigin {

    /**
     * Служебный источник-адаптер, который Spring Boot кладёт поверх остальных.
     * Он ничего не хранит сам — только приводит имена ключей к «расслабленному»
     * виду, и в отчёте лишний: иначе любой ключ выглядел бы найденным дважды.
     */
    private static final String AGGREGATING = "configurationProperties";

    /**
     * Окружение.
     */
    private final ConfigurableEnvironment environment;

    /**
     * Основной конструктор.
     * @param environment Окружение
     */
    public ConfigurationOrigin(final ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Первый источник, в котором есть ключ, — именно его значение и увидит
     * приложение. Порядок в списке источников и есть приоритет.
     * @param key Ключ
     * @return Первый источник, в котором есть ключ, — именно его значение и увидит приложение. Порядок в списке источников и есть приоритет
     */
    public Optional<Origin> resolve(final String key) {
        return this.occurrences(key).stream().findFirst();
    }

    /**
     * <b>Все</b> источники, где встречается ключ, в порядке приоритета.
     * Первый выигрывает, остальные перекрыты — именно это и надо видеть,
     * когда значение оказалось не тем, что ожидалось.
     * @param key Ключ
     * @return Первый выигрывает, остальные перекрыты — именно это и надо видеть, когда значение оказалось не тем, что ожидалось
     */
    public List<Origin> occurrences(final String key) {
        final List<Origin> found = new ArrayList<>();
        int priority = 0;
        for (final PropertySource<?> source : this.environment.getPropertySources()) {
            if (this.real(source)) {
                if (source.containsProperty(key)) {
                    found.add(new Origin(source.getName(), source.getProperty(key), priority));
                }
                priority++;
            }
        }
        return List.copyOf(found);
    }

    /**
     * Перекрыто ли значение более приоритетным источником.
     * @param key Ключ
     * @return Перекрыто ли значение более приоритетным источником
     */
    public boolean overridden(final String key) {
        return this.occurrences(key).size() > 1;
    }

    /**
     * Стек источников сверху вниз — визуализация СХЕМЫ 10.
     * @return Стек источников сверху вниз — визуализация СХЕМЫ 10
     */
    public List<String> stack() {
        final List<String> names = new ArrayList<>();
        this.environment.getPropertySources().stream()
            .filter(this::real)
            .forEach(source -> names.add(source.getName()));
        return List.copyOf(names);
    }

    /**
     * Эффективная конфигурация по префиксу: то, что реально увидит приложение,
     * с указанием источника каждого значения.
     * @param prefix Префикс
     * @return Эффективная конфигурация по префиксу: то, что реально увидит приложение, с указанием источника каждого значения
     */
    public Map<String, Origin> effective(final String prefix) {
        final Map<String, Origin> collected = new LinkedHashMap<>();
        for (final PropertySource<?> source : this.environment.getPropertySources()) {
            if (!this.real(source) || !(source instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            for (final String name : enumerable.getPropertyNames()) {
                if (name.startsWith(prefix)) {
                    collected.computeIfAbsent(name, key -> this.resolve(key).orElseThrow());
                }
            }
        }
        return Map.copyOf(collected);
    }

    /**
     * Человекочитаемое объяснение — то, что стоит напечатать в лог при старте.
     * @param key Ключ
     * @return Человекочитаемое объяснение — то, что стоит напечатать в лог при старте
     */
    public String explain(final String key) {
        final List<Origin> found = this.occurrences(key);
        if (found.isEmpty()) {
            return String.format("'%s' не найден ни в одном источнике", key);
        }
        final StringBuilder text = new StringBuilder(
            String.format("'%s' = %s (из %s)", key, found.get(0).value(), found.get(0).source())
        );
        for (int index = 1; index < found.size(); index++) {
            text.append("\n  перекрыто: ").append(found.get(index).value())
                .append(" из ").append(found.get(index).source());
        }
        return text.toString();
    }

    // @checkstyle NonStaticMethodCheck (3 lines)
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    private boolean real(final PropertySource<?> source) {
        return !AGGREGATING.equals(source.getName());
    }
}

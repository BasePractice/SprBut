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
 * <p>
 * Инструмент, отвечающий на вопрос «откуда взялось это значение» — прямая
 * реализация СХЕМЫ 10 (слайд 138). Приоритеты со слайда перестают быть списком,
 * который надо помнить: {@code Environment} показывает весь стек источников
 * целиком и говорит, какой из них победил.
 * <p>
 * Без такого инструмента вопрос «почему приложение слушает не тот порт»
 * решается перебором.
 */
public final class ConfigurationOrigin {

    /**
     * Служебный источник-адаптер, который Spring Boot кладёт поверх остальных.
     * Он ничего не хранит сам — только приводит имена ключей к «расслабленному»
     * виду, и в отчёте лишний: иначе любой ключ выглядел бы найденным дважды.
     */
    private static final String AGGREGATING = "configurationProperties";

    private final ConfigurableEnvironment environment;

    public ConfigurationOrigin(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Первый источник, в котором есть ключ, — именно его значение и увидит
     * приложение. Порядок в списке источников и есть приоритет.
     */
    public Optional<Origin> resolve(String key) {
        return occurrences(key).stream().findFirst();
    }

    /**
     * <b>Все</b> источники, где встречается ключ, в порядке приоритета.
     * Первый выигрывает, остальные перекрыты — именно это и надо видеть,
     * когда значение оказалось не тем, что ожидалось.
     */
    public List<Origin> occurrences(String key) {
        List<Origin> found = new ArrayList<>();
        int priority = 0;
        for (PropertySource<?> source : this.environment.getPropertySources()) {
            if (real(source)) {
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
     */
    public boolean overridden(String key) {
        return occurrences(key).size() > 1;
    }

    /**
     * Стек источников сверху вниз — визуализация СХЕМЫ 10.
     */
    public List<String> stack() {
        List<String> names = new ArrayList<>();
        this.environment.getPropertySources().stream()
            .filter(this::real)
            .forEach(source -> names.add(source.getName()));
        return List.copyOf(names);
    }

    /**
     * Эффективная конфигурация по префиксу: то, что реально увидит приложение,
     * с указанием источника каждого значения.
     */
    public Map<String, Origin> effective(String prefix) {
        Map<String, Origin> collected = new LinkedHashMap<>();
        for (PropertySource<?> source : this.environment.getPropertySources()) {
            if (!real(source) || !(source instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            for (String name : enumerable.getPropertyNames()) {
                if (name.startsWith(prefix)) {
                    collected.computeIfAbsent(name, key -> resolve(key).orElseThrow());
                }
            }
        }
        return Map.copyOf(collected);
    }

    /**
     * Человекочитаемое объяснение — то, что стоит напечатать в лог при старте.
     */
    public String explain(String key) {
        List<Origin> found = occurrences(key);
        if (found.isEmpty()) {
            return "'" + key + "' не найден ни в одном источнике";
        }
        StringBuilder text = new StringBuilder(
            "'" + key + "' = " + found.get(0).value() + " (из " + found.get(0).source() + ")"
        );
        for (int index = 1; index < found.size(); index++) {
            text.append("\n  перекрыто: ").append(found.get(index).value())
                .append(" из ").append(found.get(index).source());
        }
        return text.toString();
    }

    private boolean real(PropertySource<?> source) {
        return !AGGREGATING.equals(source.getName());
    }
}

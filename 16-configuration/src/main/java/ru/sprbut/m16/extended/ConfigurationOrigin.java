package ru.sprbut.m16.extended;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Расширенный пример модуля 16.</b>
 * <p>
 * Инструмент, отвечающий на вопрос «откуда взялось это значение» — прямая
 * реализация СХЕМЫ 10 (слайд 138, «приоритеты конфигурации: стек, что перекрывает что»).
 * <p>
 * {@code Environment} в Spring — это <b>упорядоченный список источников</b>.
 * Поиск идёт сверху вниз и останавливается на первом совпадении. Отсюда и весь
 * порядок приоритетов со слайдов 133–136:
 * <pre>
 *   аргументы командной строки  (--sprbut.server.port=9090)   ← перекрывает всё
 *   системные свойства          (-Dsprbut.server.port=9090)
 *   переменные окружения        (SPRBUT_SERVER_PORT=9090)
 *   application-{profile}.yaml
 *   application.yaml
 *   значения по умолчанию в коде                              ← перекрывается всем
 * </pre>
 * Без такого инструмента вопрос «почему приложение слушает не тот порт»
 * решается перебором.
 */
public final class ConfigurationOrigin {

    private ConfigurationOrigin() {
    }

    /**
     * Служебный источник-адаптер, который Spring Boot кладёт поверх всех
     * остальных. Он ничего не хранит сам — только приводит имена ключей
     * к «расслабленному» виду. В отчёте о происхождении значений он лишний:
     * иначе любой ключ выглядел бы найденным дважды.
     */
    static final String AGGREGATING_SOURCE = "configurationProperties";

    static boolean isRealSource(PropertySource<?> source) {
        return !AGGREGATING_SOURCE.equals(source.getName());
    }

    /** Где нашлось значение и что именно там лежит. */
    public record Origin(String propertySource, Object value, int priority) {
    }

    /**
     * Первый источник, в котором есть ключ, — именно его значение и увидит
     * приложение. Порядок в списке источников и есть приоритет.
     */
    public static Optional<Origin> resolve(ConfigurableEnvironment environment, String key) {
        int priority = 0;
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (!isRealSource(source)) {
                continue;
            }
            if (source.containsProperty(key)) {
                return Optional.of(new Origin(source.getName(), source.getProperty(key), priority));
            }
            priority++;
        }
        return Optional.empty();
    }

    /**
     * <b>Все</b> источники, где встречается ключ, в порядке приоритета.
     * Первый выигрывает, остальные перекрыты. Именно это и надо видеть,
     * когда значение оказалось не тем, что ожидалось.
     */
    public static List<Origin> allOccurrences(ConfigurableEnvironment environment, String key) {
        List<Origin> found = new ArrayList<>();
        int priority = 0;
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (!isRealSource(source)) {
                continue;
            }
            if (source.containsProperty(key)) {
                found.add(new Origin(source.getName(), source.getProperty(key), priority));
            }
            priority++;
        }
        return found;
    }

    /** Перекрыто ли значение более приоритетным источником. */
    public static boolean isOverridden(ConfigurableEnvironment environment, String key) {
        return allOccurrences(environment, key).size() > 1;
    }

    /** Стек источников сверху вниз — визуализация СХЕМЫ 10. */
    public static List<String> priorityStack(ConfigurableEnvironment environment) {
        List<String> stack = new ArrayList<>();
        environment.getPropertySources().stream()
                .filter(ConfigurationOrigin::isRealSource)
                .forEach(source -> stack.add(source.getName()));
        return stack;
    }

    /**
     * Эффективная конфигурация по префиксу: то, что реально увидит приложение,
     * с указанием источника каждого значения.
     */
    public static Map<String, Origin> effectiveConfig(ConfigurableEnvironment environment,
                                                      String prefix) {
        Map<String, Origin> result = new LinkedHashMap<>();
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (!isRealSource(source) || !(source instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            for (String name : enumerable.getPropertyNames()) {
                if (name.startsWith(prefix)) {
                    // первый найденный источник выигрывает — дальше не перезаписываем
                    result.computeIfAbsent(name, key -> resolve(environment, key).orElseThrow());
                }
            }
        }
        return result;
    }

    /** Человекочитаемое объяснение — то, что стоит напечатать в лог при старте. */
    public static String explain(ConfigurableEnvironment environment, String key) {
        List<Origin> occurrences = allOccurrences(environment, key);
        if (occurrences.isEmpty()) {
            return "'" + key + "' не найден ни в одном источнике";
        }
        StringBuilder sb = new StringBuilder("'" + key + "' = " + occurrences.get(0).value()
                + " (из " + occurrences.get(0).propertySource() + ")");
        for (int i = 1; i < occurrences.size(); i++) {
            Origin overridden = occurrences.get(i);
            sb.append("\n  перекрыто: ").append(overridden.value())
                    .append(" из ").append(overridden.propertySource());
        }
        return sb.toString();
    }
}

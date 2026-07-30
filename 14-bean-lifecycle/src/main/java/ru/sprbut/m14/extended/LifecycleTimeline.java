package ru.sprbut.m14.extended;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.sprbut.m14.LifecycleLog;

/**
 * <b>Расширенный пример модуля 14.</b>
 * <p>
 * Временная шкала жизненного цикла: собирает журнал в наглядный вид
 * и <b>проверяет инварианты</b> восьми шагов со слайда 118 (СХЕМА 7).
 * <p>
 * Это не украшательство, а рабочий инструмент: ровно эти инварианты нарушаются
 * в типичных ошибках — обращение к зависимости в конструкторе, запуск фонового
 * потока в {@code @PostConstruct} вместо {@code SmartLifecycle.start}, ожидание
 * {@code @PreDestroy} у prototype-бина, недоумение по поводу прокси вместо
 * своего объекта.
 */
public final class LifecycleTimeline {

    private final LifecycleLog log;

    public LifecycleTimeline() {
        this(new LifecycleLog());
    }

    public LifecycleTimeline(LifecycleLog log) {
        this.log = log;
    }

    /**
     * Журнал, разобранный в список шагов.
     */
    public List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        for (String event : this.log.events()) {
            int colon = event.indexOf(':');
            String left = event.substring(0, colon);
            steps.add(new Step(
                Character.getNumericValue(left.charAt(0)),
                left.substring(left.indexOf('-') + 1),
                event.substring(colon + 1)
            ));
        }
        return List.copyOf(steps);
    }

    /**
     * Шаги одного бина в порядке выполнения.
     */
    public List<Step> of(String bean) {
        return steps().stream().filter(step -> step.bean().equals(bean)).toList();
    }

    /**
     * Наглядная шкала — то, что имеет смысл распечатать при отладке.
     */
    public String render(String bean) {
        StringBuilder text = new StringBuilder("Жизненный цикл '").append(bean).append("':\n");
        for (Step step : of(bean)) {
            text.append("  ").append(step).append('\n');
        }
        return text.toString();
    }

    /**
     * Нарушения контракта контейнера; пустой список означает, что порядок верен.
     */
    public List<Violation> violations(String bean) {
        List<Step> steps = of(bean);
        if (steps.isEmpty()) {
            return List.of(
                new Violation("нет данных", "бин '" + bean + "' не встречается в журнале")
            );
        }
        List<Violation> found = new ArrayList<>();
        for (int index = 1; index < steps.size(); index++) {
            if (steps.get(index).number() < steps.get(index - 1).number()) {
                found.add(new Violation(
                    "порядок шагов",
                    steps.get(index) + " выполнен после " + steps.get(index - 1)
                ));
            }
        }
        precedes(steps, "constructor", "dependencies", found);
        precedes(steps, "dependencies", "aware-beanName", found);
        precedes(steps, "aware-applicationContext", "bpp-before", found);
        precedes(steps, "bpp-before", "postConstruct", found);
        precedes(steps, "postConstruct", "afterPropertiesSet", found);
        precedes(steps, "afterPropertiesSet", "bpp-after", found);
        precedes(steps, "preDestroy", "destroy", found);
        return List.copyOf(found);
    }

    /**
     * Сводка «бин — сколько шагов жизненного цикла он прошёл».
     */
    public Map<String, Integer> summary() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Step step : steps()) {
            counts.merge(step.bean(), 1, Integer::sum);
        }
        return Map.copyOf(counts);
    }

    /**
     * Дошёл ли бин до фазы уничтожения.
     */
    public boolean destroyed(String bean) {
        return of(bean).stream().anyMatch(
            step -> step.phase().startsWith("preDestroy") || step.phase().startsWith("destroy")
        );
    }

    private void precedes(List<Step> steps, String earlier, String later, List<Violation> sink) {
        int first = position(steps, earlier);
        int second = position(steps, later);
        if (first < 0 || second < 0) {
            return;
        }
        if (first > second) {
            sink.add(new Violation(
                earlier + " перед " + later,
                earlier + " на позиции " + first + ", " + later + " на " + second
            ));
        }
    }

    private int position(List<Step> steps, String phase) {
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).phase().equals(phase)) {
                return index;
            }
        }
        return -1;
    }
}

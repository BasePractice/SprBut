package ru.sprbut.m14.extended;

import ru.sprbut.m14.LifecycleLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Расширенный пример модуля 14.</b>
 * <p>
 * Временная шкала жизненного цикла: собирает журнал в наглядный вид
 * и <b>проверяет инварианты</b> восьми шагов со слайда 118 (СХЕМА 7).
 * <p>
 * Это не украшательство, а рабочий инструмент. Ровно эти инварианты
 * нарушаются в типичных ошибках:
 * <ul>
 *   <li>обращение к зависимости в конструкторе — она ещё не внедрена;</li>
 *   <li>запуск фонового потока в {@code @PostConstruct} — контекст ещё не готов,
 *       для этого есть {@code SmartLifecycle.start};</li>
 *   <li>ожидание {@code @PreDestroy} у prototype-бина — его не будет никогда;</li>
 *   <li>непонимание, почему в контексте лежит прокси, а не ваш объект —
 *       его подменил {@code BeanPostProcessor} на шаге 6.</li>
 * </ul>
 */
public final class LifecycleTimeline {

    private LifecycleTimeline() {
    }

    /** Шаг жизненного цикла: номер, название, бин. */
    public record Step(int number, String phase, String bean) {

        @Override
        public String toString() {
            return number + ". " + phase + " → " + bean;
        }
    }

    /** Нарушение ожидаемого порядка. */
    public record Violation(String rule, String detail) {
    }

    /** Разбирает журнал в список шагов. */
    public static List<Step> steps() {
        List<Step> steps = new ArrayList<>();
        for (String event : LifecycleLog.events()) {
            int colon = event.indexOf(':');
            String left = event.substring(0, colon);
            String bean = event.substring(colon + 1);
            int dash = left.indexOf('-');
            int number = Character.getNumericValue(left.charAt(0));
            steps.add(new Step(number, left.substring(dash + 1), bean));
        }
        return steps;
    }

    /** Шаги одного бина в порядке выполнения. */
    public static List<Step> stepsOf(String bean) {
        return steps().stream().filter(s -> s.bean().equals(bean)).toList();
    }

    /** Наглядная шкала — то, что имеет смысл распечатать при отладке. */
    public static String render(String bean) {
        StringBuilder sb = new StringBuilder("Жизненный цикл '" + bean + "':\n");
        for (Step step : stepsOf(bean)) {
            sb.append("  ").append(step).append('\n');
        }
        return sb.toString();
    }

    /**
     * Проверка инвариантов. Пустой список означает, что порядок соответствует
     * контракту контейнера.
     */
    public static List<Violation> validate(String bean) {
        List<Violation> violations = new ArrayList<>();
        List<Step> steps = stepsOf(bean);
        if (steps.isEmpty()) {
            return List.of(new Violation("нет данных", "бин '" + bean + "' не встречается в журнале"));
        }

        List<Integer> numbers = steps.stream().map(Step::number).toList();
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) < numbers.get(i - 1)) {
                violations.add(new Violation("порядок шагов",
                        steps.get(i) + " выполнен после " + steps.get(i - 1)));
            }
        }

        checkPrecedes(steps, "constructor", "dependencies", violations);
        checkPrecedes(steps, "dependencies", "aware-beanName", violations);
        checkPrecedes(steps, "aware-applicationContext", "bpp-before", violations);
        checkPrecedes(steps, "bpp-before", "postConstruct", violations);
        checkPrecedes(steps, "postConstruct", "afterPropertiesSet", violations);
        checkPrecedes(steps, "afterPropertiesSet", "bpp-after", violations);
        checkPrecedes(steps, "preDestroy", "destroy", violations);

        return violations;
    }

    private static void checkPrecedes(List<Step> steps, String earlier, String later,
                                      List<Violation> sink) {
        int earlierIndex = indexOfPhase(steps, earlier);
        int laterIndex = indexOfPhase(steps, later);
        if (earlierIndex < 0 || laterIndex < 0) {
            return;
        }
        if (earlierIndex > laterIndex) {
            sink.add(new Violation(earlier + " перед " + later,
                    earlier + " на позиции " + earlierIndex + ", " + later + " на " + laterIndex));
        }
    }

    private static int indexOfPhase(List<Step> steps, String phase) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).phase().equals(phase)) {
                return i;
            }
        }
        return -1;
    }

    /** Сводка «бин → сколько шагов жизненного цикла он прошёл». */
    public static Map<String, Integer> summary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        for (Step step : steps()) {
            summary.merge(step.bean(), 1, Integer::sum);
        }
        return summary;
    }

    /** Дошёл ли бин до фазы уничтожения. */
    public static boolean wasDestroyed(String bean) {
        return stepsOf(bean).stream()
                .anyMatch(s -> s.phase().startsWith("preDestroy") || s.phase().startsWith("destroy"));
    }
}

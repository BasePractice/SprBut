/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14.extended;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.sprbut.m14.LifecycleLog;

/**
 * <b>Расширенный пример модуля 14.</b>
 *
 * <p>Временная шкала жизненного цикла: собирает журнал в наглядный вид
 * и <b>проверяет инварианты</b> восьми шагов со слайда 118 (СХЕМА 7).</p>
 *
 * <p>Это не украшательство, а рабочий инструмент: ровно эти инварианты нарушаются
 * в типичных ошибках — обращение к зависимости в конструкторе, запуск фонового
 * потока в {@code @PostConstruct} вместо {@code SmartLifecycle.start}, ожидание
 * {@code @PreDestroy} у prototype-бина, недоумение по поводу прокси вместо
 * своего объекта.</p>
 *
 * @since 1.0
 */
public final class LifecycleTimeline {

    /**
     * Журнал.
     */
    private final LifecycleLog log;

    /**
     * Основной конструктор.
     */
    public LifecycleTimeline() {
        this(new LifecycleLog());
    }

    /**
     * Основной конструктор.
     * @param log Журнал
     */
    public LifecycleTimeline(final LifecycleLog log) {
        this.log = log;
    }

    /**
     * Журнал, разобранный в список шагов.
     * @return Журнал, разобранный в список шагов
     */
    public List<Step> steps() {
        final List<Step> steps = new ArrayList<>(0);
        for (final String event : this.log.events()) {
            final int colon = event.indexOf(':');
            final String left = event.substring(
                0, colon
            );
            steps.add(new Step(
                Character.getNumericValue(left.charAt(0)),
                left.substring(left.indexOf('-') + 1),
                event.substring(
                    colon + 1
                )
            ));
        }
        return List.copyOf(steps);
    }

    /**
     * Шаги одного бина в порядке выполнения.
     * @param bean Объект
     * @return Шаги одного бина в порядке выполнения
     */
    public List<Step> of(final String bean) {
        return this.steps().stream().filter(step -> step.bean().equals(bean)).toList();
    }

    /**
     * Наглядная шкала — то, что имеет смысл распечатать при отладке.
     * @param bean Объект
     * @return Наглядная шкала — то, что имеет смысл распечатать при отладке
     */
    public String render(final String bean) {
        final StringBuilder text = new StringBuilder("Жизненный цикл '").append(bean).append("':\n");
        for (final Step step : this.of(bean)) {
            text.append("  ").append(step).append('\n');
        }
        return text.toString();
    }

    /**
     * Нарушения контракта контейнера; пустой список означает, что порядок верен.
     * @param bean Объект
     * @return Нарушения контракта контейнера; пустой список означает, что порядок верен
     */
    public List<Violation> violations(final String bean) {
        final List<Step> steps = this.of(bean);
        if (steps.isEmpty()) {
            return List.of(
                new Violation("нет данных", "бин '" + bean + "' не встречается в журнале")
            );
        }
        final List<Violation> found = new ArrayList<>(0);
        for (int index = 1; index < steps.size(); index++) {
            if (
                steps.get(
                    index
                ).number() < steps.get(
                    index - 1
                ).number()
            ) {
                found.add(new Violation(
                    "порядок шагов",
                    steps.get(
                        index
                    ) + " выполнен после " + steps.get(
                        index - 1
                    )
                ));
            }
        }
        this.precedes(steps, "constructor", "dependencies", found);
        this.precedes(steps, "dependencies", "aware-beanName", found);
        this.precedes(steps, "aware-applicationContext", "bpp-before", found);
        this.precedes(steps, "bpp-before", "postConstruct", found);
        this.precedes(steps, "postConstruct", "afterPropertiesSet", found);
        this.precedes(steps, "afterPropertiesSet", "bpp-after", found);
        this.precedes(steps, "preDestroy", "destroy", found);
        return List.copyOf(found);
    }

    /**
     * Сводка «бин — сколько шагов жизненного цикла он прошёл».
     * @return Сводка «бин — сколько шагов жизненного цикла он прошёл»
     */
    public Map<String, Integer> summary() {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        for (final Step step : this.steps()) {
            counts.merge(step.bean(), 1, Integer::sum);
        }
        return Map.copyOf(counts);
    }

    /**
     * Дошёл ли бин до фазы уничтожения.
     * @param bean Объект
     * @return Дошёл ли бин до фазы уничтожения
     */
    public boolean destroyed(final String bean) {
        return this.of(bean).stream().anyMatch(
            step -> step.phase().startsWith("preDestroy") || step.phase().startsWith("destroy")
        );
    }

    private void precedes(final List<Step> steps, final String earlier, final String later, final List<Violation> sink) {
        final int first = this.position(steps, earlier);
        final int second = this.position(steps, later);
        if (first < 0 || second < 0) {
            return;
        }
        if (
            first > second
        ) {
            sink.add(new Violation(
                String.format("%s перед %s", earlier, later),
                String.format(
                    "%s на позиции %s, %s на %s", earlier, first, later, second
                )
            ));
        }
    }

    private static int position(final List<Step> steps, final String phase) {
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).phase().equals(phase)) {
                return index;
            }
        }
        return -1;
    }
}

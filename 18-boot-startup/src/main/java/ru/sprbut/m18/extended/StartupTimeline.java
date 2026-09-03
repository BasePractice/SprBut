/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m18.extended;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.sprbut.m18.StartupLog;

/**
 * <b>Расширенный пример модуля 18.</b>
 *
 * <p>Восстановление последовательности запуска — прямая реализация СХЕМЫ 11
 * (слайд 172, «sequence-диаграмма: от run() через события к ApplicationReadyEvent»).</p>
 *
 * <p>Главная практическая ценность — {@link #whereToHook}: справочник «что уже
 * готово в каждой точке». Без него выбор места для своего кода превращается
 * в угадывание, а ошибка проявляется как {@code NullPointerException} на старте
 * или, что хуже, как молчаливо неинициализированный компонент.</p>
 *
 * @since 1.0
 */
public final class StartupTimeline {

    /**
     * Журнал.
     */
    private final StartupLog log;

    /**
     * Основной конструктор.
     */
    public StartupTimeline() {
        this(new StartupLog());
    }

    /**
     * Основной конструктор.
     * @param log Журнал
     */
    public StartupTimeline(final StartupLog log) {
        this.log = log;
    }

    /**
     * Канонический порядок точек расширения при запуске.
     * @return Канонический порядок точек расширения
     * @checkstyle NonStaticMethodCheck (4 lines)
     */
    public List<HookPoint> whereToHook() {
        return List.of(
            new HookPoint(
                1, "ApplicationStartingEvent", "ничего: ни Environment, ни контекста",
                "инициализация логирования"
            ),
            new HookPoint(
                2, "ApplicationEnvironmentPreparedEvent", "Environment собран, контекста нет",
                "добавить свой источник настроек, включить профиль"
            ),
            new HookPoint(
                3, "ApplicationContextInitializer", "контекст создан, но пуст",
                "программная настройка контекста до загрузки бинов"
            ),
            new HookPoint(
                4, "ApplicationContextInitializedEvent",
                "инициализаторы отработали, определений бинов ещё нет", "ранняя диагностика"
            ),
            new HookPoint(
                5, "ApplicationPreparedEvent", "определения бинов загружены, объектов нет",
                "последний момент правки BeanDefinition"
            ),
            new HookPoint(
                6, "BeanFactoryPostProcessor", "все определения бинов на руках",
                "переопределить или добавить определение бина"
            ),
            new HookPoint(
                7, "ContextRefreshedEvent", "все синглтоны созданы, контекст поднят",
                "проверки целостности, прогрев кэшей"
            ),
            new HookPoint(
                8, "ApplicationStartedEvent", "контекст поднят, раннеры ещё не выполнялись",
                "метрики времени старта"
            ),
            new HookPoint(
                9, "ApplicationRunner / CommandLineRunner", "приложение работоспособно",
                "разовые задачи при старте, миграции, загрузка справочников"
            ),
            new HookPoint(
                10, "ApplicationReadyEvent", "готово всё, включая раннеры",
                "сообщить, что приложение принимает нагрузку"
            )
        );
    }

    /**
     * Перехватчик.
     * @param name Имя
     * @return Перехватчик
     */
    public Optional<HookPoint> hook(final String name) {
        return this.whereToHook().stream()
            .filter(point -> point.name().startsWith(name))
            .findFirst();
    }

    /**
     * Фактическая последовательность, восстановленная из журнала.
     * @return Фактическая последовательность, восстановленная из журнала
     */
    public List<String> actualSequence() {
        return this.log.events().stream()
            .map(StartupTimeline::phaseOf)
            .distinct()
            .toList();
    }

    /**
     * Номера шагов в порядке их фактического выполнения.
     * @return Номера шагов в порядке их фактического выполнения
     */
    public List<Integer> actualOrder() {
        return this.log.events().stream()
            .map(StartupTimeline::orderOf)
            .filter(order -> order > 0)
                .distinct()
                .toList();
    }

    /**
     * Не нарушен ли порядок: номера шагов должны только возрастать.
     * @return Признак того, что порядок шагов не нарушен
     */
    public boolean isOrdered() {
        final List<Integer> order = this.actualOrder();
        boolean sorted = true;
        for (int index = 1; index < order.size(); index += 1) {
            if (order.get(index) < order.get(index - 1)) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    /**
     * Наглядная диаграмма — то, что стоит распечатать при разборе старта.
     * @return Наглядная диаграмма запуска
     */
    public String render() {
        final StringBuilder text = new StringBuilder(120).append(
            String.format("SpringApplication.run()%n")
        );
        for (final String event : this.log.events()) {
            text.append("  | ").append(event).append(String.format("%n"));
        }
        text.append("  v приложение готово");
        return text.toString();
    }

    /**
     * Сколько раз встретился каждый шаг.
     * @return Сколько раз встретился каждый шаг
     */
    public Map<String, Long> counts() {
        final Map<String, Long> counts = new LinkedHashMap<>();
        this.log.events()
            .forEach(event -> counts.merge(StartupTimeline.phaseOf(event), 1L, Long::sum));
        return counts;
    }

    private static String phaseOf(final String event) {
        final int dash = event.indexOf('-');
        final String tail;
        if (dash < 0) {
            tail = event;
        } else {
            tail = event.substring(dash + 1);
        }
        final int colon = tail.indexOf(':');
        final String phase;
        if (colon < 0) {
            phase = tail;
        } else {
            phase = tail.substring(0, colon);
        }
        return phase;
    }

    private static int orderOf(final String event) {
        int end = 0;
        while (end < event.length() && Character.isDigit(event.charAt(end))) {
            end += 1;
        }
        final int order;
        if (end == 0) {
            order = -1;
        } else {
            order = Integer.parseInt(event.substring(0, end));
        }
        return order;
    }
}

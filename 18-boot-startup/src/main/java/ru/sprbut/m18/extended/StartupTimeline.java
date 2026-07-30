package ru.sprbut.m18.extended;

import ru.sprbut.m18.StartupLog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Расширенный пример модуля 18.</b>
 * <p>
 * Восстановление последовательности запуска — прямая реализация СХЕМЫ 11
 * (слайд 172, «sequence-диаграмма: run() → события → ApplicationReadyEvent»).
 * <p>
 * Главная практическая ценность — {@link #whereToHook}: справочник «что уже
 * готово в каждой точке». Без него выбор места для своего кода превращается
 * в угадывание, а ошибка проявляется как {@code NullPointerException} на старте
 * или, что хуже, как молчаливо неинициализированный компонент.
 */
public final class StartupTimeline {

    private final StartupLog log;

    public StartupTimeline() {
        this(new StartupLog());
    }

    public StartupTimeline(StartupLog log) {
        this.log = log;
    }

    /** Точка расширения: что уже готово и что здесь принято делать. */
    /** Канонический порядок точек расширения при запуске Spring Boot. */
    public List<HookPoint> whereToHook() {
        return List.of(
                new HookPoint(1, "ApplicationStartingEvent",
                        "ничего: ни Environment, ни контекста",
                        "инициализация логирования"),
                new HookPoint(2, "ApplicationEnvironmentPreparedEvent",
                        "Environment собран, контекста нет",
                        "добавить свой источник настроек, включить профиль"),
                new HookPoint(3, "ApplicationContextInitializer",
                        "контекст создан, но пуст",
                        "программная настройка контекста до загрузки бинов"),
                new HookPoint(4, "ApplicationContextInitializedEvent",
                        "инициализаторы отработали, определений бинов ещё нет",
                        "ранняя диагностика"),
                new HookPoint(5, "ApplicationPreparedEvent",
                        "определения бинов загружены, объектов нет",
                        "последний момент правки BeanDefinition"),
                new HookPoint(6, "BeanFactoryPostProcessor",
                        "все определения бинов на руках",
                        "переопределить или добавить определение бина"),
                new HookPoint(7, "ContextRefreshedEvent",
                        "все синглтоны созданы, контекст поднят",
                        "проверки целостности, прогрев кэшей"),
                new HookPoint(8, "ApplicationStartedEvent",
                        "контекст поднят, раннеры ещё не выполнялись",
                        "метрики времени старта"),
                new HookPoint(9, "ApplicationRunner / CommandLineRunner",
                        "приложение работоспособно",
                        "разовые задачи при старте, миграции, загрузка справочников"),
                new HookPoint(10, "ApplicationReadyEvent",
                        "готово всё, включая раннеры",
                        "сообщить, что приложение принимает нагрузку"));
    }

    public Optional<HookPoint> hook(String name) {
        return whereToHook().stream().filter(point -> point.name().startsWith(name)).findFirst();
    }

    /** Фактическая последовательность, восстановленная из журнала. */
    public List<String> actualSequence() {
        return this.log.events().stream()
                .map(this::phaseOf)
                .distinct()
                .toList();
    }

    /** Номера шагов в порядке их фактического выполнения. */
    public List<Integer> actualOrder() {
        return this.log.events().stream()
                .map(this::orderOf)
                .filter(order -> order > 0)
                .distinct()
                .toList();
    }

    /** Не нарушен ли порядок: номера шагов должны только возрастать. */
    public boolean isOrdered() {
        List<Integer> order = actualOrder();
        for (int i = 1; i < order.size(); i++) {
            if (order.get(i) < order.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    /** Наглядная диаграмма — то, что стоит распечатать при разборе старта. */
    public String render() {
        StringBuilder sb = new StringBuilder("SpringApplication.run()\n");
        for (String event : this.log.events()) {
            sb.append("  │ ").append(event).append('\n');
        }
        sb.append("  ▼ приложение готово");
        return sb.toString();
    }

    /** Сколько раз встретился каждый шаг. */
    public Map<String, Long> counts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        this.log.events().forEach(event -> counts.merge(phaseOf(event), 1L, Long::sum));
        return counts;
    }

    private String phaseOf(String event) {
        int dash = event.indexOf('-');
        String tail = dash < 0 ? event : event.substring(dash + 1);
        int colon = tail.indexOf(':');
        return colon < 0 ? tail : tail.substring(0, colon);
    }

    /**
     * Номер шага — <b>все</b> ведущие цифры, а не первая.
     * Иначе «10-ApplicationReadyEvent» превратился бы в шаг 1 и встал в начало.
     */
    private int orderOf(String event) {
        int end = 0;
        while (end < event.length() && Character.isDigit(event.charAt(end))) {
            end++;
        }
        return end == 0 ? -1 : Integer.parseInt(event.substring(0, end));
    }
}

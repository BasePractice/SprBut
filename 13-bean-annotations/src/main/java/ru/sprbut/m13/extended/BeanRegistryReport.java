package ru.sprbut.m13.extended;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Расширенный пример модуля 13.</b>
 * <p>
 * Отчёт о содержимом контейнера: что в нём есть, с каким скоупом, что помечено
 * {@code @Primary}, что ленивое, что уже создано, а что ещё нет.
 * <p>
 * Практическая ценность — диагностика. Ровно эти вопросы возникают, когда
 * приложение ведёт себя не так, как ожидалось:
 * <ul>
 *   <li>«почему внедрился не тот бин» → {@link Entry#primary()} и список кандидатов;</li>
 *   <li>«почему бин создался дважды» → {@link Entry#scope()};</li>
 *   <li>«почему бин не создался при старте» → {@link Entry#lazyInit()};</li>
 *   <li>«почему бин вообще не появился» → его просто нет в отчёте (условие
 *       {@code @Conditional} или {@code @Profile} не выполнилось).</li>
 * </ul>
 * Spring Boot делает то же самое в отчёте об условиях по флагу {@code --debug}
 * (модуль 19).
 */
public final class BeanRegistryReport {

    private BeanRegistryReport() {
    }

    /** Строка отчёта об одном бине. */
    public record Entry(String name,
                        String type,
                        String scope,
                        boolean primary,
                        boolean lazyInit,
                        boolean instantiated,
                        List<String> dependsOn) {

        public Entry {
            dependsOn = List.copyOf(dependsOn);
        }

        public boolean singleton() {
            return BeanDefinition.SCOPE_SINGLETON.equals(scope) || scope.isEmpty();
        }
    }

    /** Весь отчёт, отсортированный по имени бина. */
    public static List<Entry> of(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        List<Entry> entries = new ArrayList<>();

        for (String name : factory.getBeanDefinitionNames()) {
            BeanDefinition definition = factory.getBeanDefinition(name);
            Class<?> type = factory.getType(name);
            entries.add(new Entry(
                    name,
                    type == null ? "?" : type.getSimpleName(),
                    definition.getScope(),
                    definition.isPrimary(),
                    definition.isLazyInit(),
                    factory.containsSingleton(name),
                    definition.getDependsOn() == null
                            ? List.of()
                            : Arrays.asList(definition.getDependsOn())));
        }
        entries.sort(Comparator.comparing(Entry::name));
        return entries;
    }

    /** Только бины прикладных пакетов — без инфраструктуры самого Spring. */
    public static List<Entry> applicationBeans(ConfigurableApplicationContext context) {
        return of(context).stream()
                .filter(e -> !e.name().startsWith("org.springframework"))
                .toList();
    }

    /** Кандидаты на внедрение по типу — то, что контейнер увидит в точке внедрения. */
    public static List<String> candidatesFor(ConfigurableApplicationContext context, Class<?> type) {
        return Arrays.stream(context.getBeanNamesForType(type)).sorted().toList();
    }

    /**
     * Объяснение, какой бин будет выбран из нескольких кандидатов и почему.
     * Повторяет порядок разрешения, принятый в Spring.
     */
    public static String explainResolution(ConfigurableApplicationContext context, Class<?> type) {
        List<String> candidates = candidatesFor(context, type);
        if (candidates.isEmpty()) {
            return "нет кандидатов типа " + type.getSimpleName() + " → NoSuchBeanDefinitionException";
        }
        if (candidates.size() == 1) {
            return "единственный кандидат: " + candidates.get(0);
        }
        List<String> primaries = of(context).stream()
                .filter(Entry::primary)
                .filter(e -> candidates.contains(e.name()))
                .map(Entry::name)
                .toList();
        if (primaries.size() == 1) {
            return "@Primary: " + primaries.get(0) + " из " + candidates;
        }
        if (primaries.size() > 1) {
            return "несколько @Primary " + primaries + " → NoUniqueBeanDefinitionException";
        }
        return "кандидатов " + candidates.size() + " " + candidates
                + ", @Primary нет → нужен @Qualifier, иначе NoUniqueBeanDefinitionException";
    }

    /** Сводка «скоуп → сколько бинов». */
    public static Map<String, Long> scopeSummary(ConfigurableApplicationContext context) {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Entry entry : applicationBeans(context)) {
            String scope = entry.scope().isEmpty() ? BeanDefinition.SCOPE_SINGLETON : entry.scope();
            summary.merge(scope, 1L, Long::sum);
        }
        return summary;
    }

    /** Бины, которые объявлены, но ещё не созданы — то есть ленивые или прототипы. */
    public static List<String> notYetInstantiated(ConfigurableApplicationContext context) {
        return applicationBeans(context).stream()
                .filter(e -> !e.instantiated())
                .map(Entry::name)
                .toList();
    }
}

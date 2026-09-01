/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m13.extended;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * <b>Расширенный пример модуля 13.</b>
 *
 * <p>Отчёт о содержимом контейнера: что в нём есть, с каким скоупом, что помечено
 * {@code @Primary}, что ленивое, а что уже создано.</p>
 *
 * <p>Главная ценность — {@link #resolution(Class)}: он объясняет, какой бин
 * контейнер выберет из нескольких кандидатов и почему. Ровно тот вопрос,
 * на который приходится отвечать, читая {@code NoUniqueBeanDefinitionException}.
 * Spring Boot делает то же самое в отчёте об условиях по флагу {@code --debug}.</p>
 *
 * @since 1.0
 */
public final class BeanRegistryReport {

    /**
     * Контекст.
     */
    private final ConfigurableApplicationContext context;

    /**
     * Основной конструктор.
     * @param context Контекст
     */
    public BeanRegistryReport(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Весь отчёт, отсортированный по имени бина.
     * @return Весь отчёт, отсортированный по имени бина
     */
    public List<Entry> entries() {
        final ConfigurableListableBeanFactory beans = this.context.getBeanFactory();
        final List<Entry> collected = new ArrayList<>();
        for (String name : beans.getBeanDefinitionNames()) {
            final BeanDefinition definition = beans.getBeanDefinition(name);
            final Class<?> type = beans.getType(name);
            collected.add(new Entry(
                name,
                type == null ? "?" : type.getSimpleName(),
                definition.getScope(),
                definition.isPrimary(),
                definition.isLazyInit(),
                beans.containsSingleton(name),
                definition.getDependsOn() == null
                    ? List.of()
                    : Arrays.asList(definition.getDependsOn())
            ));
        }
        collected.sort(Comparator.comparing(Entry::name));
        return List.copyOf(collected);
    }

    /**
     * Только бины прикладных пакетов — без инфраструктуры самого Spring.
     * @return Только бины прикладных пакетов — без инфраструктуры самого Spring
     */
    public List<Entry> application() {
        return this.entries().stream()
            .filter(entry -> !entry.name().startsWith("org.springframework"))
            .toList();
    }

    /**
     * Кандидаты на внедрение по типу — то, что контейнер увидит в точке внедрения.
     * @param type Тип
     * @return Кандидаты на внедрение по типу — то, что контейнер увидит в точке внедрения
     */
    public List<String> candidates(final Class<?> type) {
        return Arrays.stream(this.context.getBeanNamesForType(type)).sorted().toList();
    }

    /**
     * Объяснение, какой бин будет выбран и почему.
     *
     * <p>Порядок разрешения повторяет принятый в Spring: единственный кандидат,
     * затем {@code @Primary}, затем отказ с требованием {@code @Qualifier}.</p>
     * @param type Тип
     * @return Объяснение, какой бин будет выбран и почему
     */
    public String resolution(final Class<?> type) {
        final List<String> candidates = this.candidates(type);
        if (candidates.isEmpty()) {
            return "нет кандидатов типа " + type.getSimpleName()
                + " → NoSuchBeanDefinitionException";
        }
        if (candidates.size() == 1) {
            return "единственный кандидат: " + candidates.get(0);
        }
        final List<String> primary = this.entries().stream()
            .filter(Entry::primary)
            .filter(entry -> candidates.contains(entry.name()))
            .map(Entry::name)
            .toList();
        if (primary.size() == 1) {
            return "@Primary: " + primary.get(0) + " из " + candidates;
        }
        if (primary.size() > 1) {
            return "несколько @Primary " + primary + " → NoUniqueBeanDefinitionException";
        }
        return "кандидатов " + candidates.size() + " " + candidates
            + ", @Primary нет → нужен @Qualifier, иначе NoUniqueBeanDefinitionException";
    }

    /**
     * Сводка «скоуп — сколько бинов».
     * @return Сводка «скоуп — сколько бинов»
     */
    public Map<String, Long> scopes() {
        final Map<String, Long> summary = new LinkedHashMap<>();
        for (Entry entry : this.application()) {
            summary.merge(
                entry.scope().isEmpty() ? BeanDefinition.SCOPE_SINGLETON : entry.scope(),
                1L,
                Long::sum
            );
        }
        return Map.copyOf(summary);
    }

    /**
     * Бины, которые объявлены, но ещё не созданы, — ленивые и прототипы.
     * @return Бины, которые объявлены, но ещё не созданы, — ленивые и прототипы
     */
    public List<String> pending() {
        return this.application().stream()
            .filter(entry -> !entry.instantiated())
            .map(Entry::name)
            .toList();
    }
}

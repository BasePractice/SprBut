/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// nameFragment — фрагмент имени конфигурации, по которому идёт отбор
// @checkstyle ParameterNameCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19.extended;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * <b>Расширенный пример модуля 19.</b>
 *
 * <p>Программный доступ к отчёту об условиях — тому самому, который Boot печатает
 * по флагу {@code --debug} (слайд 178).</p>
 *
 * <p>Отчёт отвечает на два вопроса, на которые иначе отвечают перебором:
 * <ul>
 * <li><b>«почему бина нет»</b> — {@link #whyExcluded} назовёт условие,
 * которое не выполнилось, дословно;</li>
 * <li><b>«почему бин есть, хотя я его не объявлял»</b> — {@link #whyIncluded}
 * покажет, какая автоконфигурация его дала.</li>
 * </ul>
 * Это не отладочная игрушка: тот же отчёт можно проверять тестом, чтобы
 * зафиксировать поведение стартера — что при таких-то настройках бин появляется,
 * а при таких-то нет.</p>
 *
 * @since 1.0
 */
public final class ConditionReport {

    /**
     * Контекст.
     */
    private final ConfigurableApplicationContext context;

    /**
     * Основной конструктор.
     * @param context Контекст
     */
    public ConditionReport(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Конфигурации, условия которых выполнились.
     * @return Конфигурации, условия которых выполнились
     */
    public List<String> included() {
        return this.evaluated().getConditionAndOutcomesBySource().entrySet().stream()
            .filter(entry -> entry.getValue().isFullMatch())
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    }

    /**
     * Конфигурации, условия которых не выполнились.
     * @return Конфигурации, условия которых не выполнились
     */
    public List<String> excluded() {
        return this.evaluated().getConditionAndOutcomesBySource().entrySet().stream()
            .filter(entry -> !entry.getValue().isFullMatch())
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    }

    /**
     * Полный отчёт по всем конфигурациям, чьё имя содержит фрагмент.
     * @param nameFragment Имя
     * @return Полный отчёт по всем конфигурациям, чьё имя содержит фрагмент
     */
    public Map<String, ConditionEntry> matching(final String nameFragment) {
        final Map<String, ConditionEntry> result = new LinkedHashMap<>(0);
        this.evaluated().getConditionAndOutcomesBySource().forEach(
            (source, outcomes) -> {
                if (source.contains(nameFragment)) {
                    final List<String> reasons = new ArrayList<>(0);
                    outcomes.forEach(
                        outcome -> reasons.add(ConditionReport.reason(outcome.getOutcome()))
                    );
                    result.put(
                        source,
                        new ConditionEntry(source, outcomes.isFullMatch(), reasons)
                    );
                }
            }
        );
        return result;
    }

    /**
     * Почему конфигурация не применилась — дословная формулировка условия.
     * @param nameFragment Имя
     * @return Дословная формулировка невыполненного условия
     */
    public Optional<String> whyExcluded(final String nameFragment) {
        return this.matching(nameFragment).values().stream()
            .filter(entry -> !entry.matched())
            .flatMap(entry -> entry.reasons().stream())
            .filter(reason -> reason.startsWith("-"))
            .findFirst();
    }

    /**
     * Почему конфигурация применилась.
     * @param nameFragment Имя
     * @return Почему конфигурация применилась
     */
    public Optional<String> whyIncluded(final String nameFragment) {
        return this.matching(nameFragment).values().stream()
            .filter(ConditionEntry::matched)
            .flatMap(entry -> entry.reasons().stream())
            .findFirst();
    }

    /**
     * Применилась ли указанная автоконфигурация.
     * @param nameFragment Имя
     * @return Применилась ли указанная автоконфигурация
     */
    public boolean applied(final String nameFragment) {
        return this.matching(nameFragment).values().stream().anyMatch(ConditionEntry::matched);
    }

    /**
     * Текстовый отчёт — то же, что печатает {@code --debug}, только по одной теме.
     * @param nameFragment Имя
     * @return Текстовый отчёт по одной теме
     */
    public String render(final String nameFragment) {
        final StringBuilder text = new StringBuilder(120).append(
            String.format("Отчёт об условиях для '%s':%n", nameFragment)
        );
        final Map<String, ConditionEntry> entries = this.matching(nameFragment);
        if (entries.isEmpty()) {
            text.append("  (нет подходящих конфигураций)");
        }
        entries.forEach(
            (source, entry) -> {
                final String verdict;
                if (entry.matched()) {
                    verdict = "ПРИМЕНЕНА";
                } else {
                    verdict = "ПРОПУЩЕНА";
                }
                text.append(String.format("  %s: %s%n", verdict, source));
                entry.reasons().forEach(
                    reason -> text.append(String.format("      %s%n", reason))
                );
            }
        );
        return text.toString();
    }

    private ConditionEvaluationReport evaluated() {
        return ConditionEvaluationReport.get(this.context.getBeanFactory());
    }

    private static String reason(final ConditionOutcome outcome) {
        final String mark;
        if (outcome.isMatch()) {
            mark = "+";
        } else {
            mark = "-";
        }
        return String.format("%s %s", mark, outcome.getMessage());
    }
}

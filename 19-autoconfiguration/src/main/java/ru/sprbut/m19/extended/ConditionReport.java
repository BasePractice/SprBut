package ru.sprbut.m19.extended;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>Расширенный пример модуля 19.</b>
 * <p>
 * Программный доступ к отчёту об условиях — тому самому, который Boot печатает
 * по флагу {@code --debug} (слайд 178).
 * <p>
 * Отчёт отвечает на два вопроса, на которые иначе отвечают перебором:
 * <ul>
 *   <li><b>«почему бина нет»</b> — {@link #whyExcluded} назовёт условие,
 *       которое не выполнилось, дословно;</li>
 *   <li><b>«почему бин есть, хотя я его не объявлял»</b> — {@link #whyIncluded}
 *       покажет, какая автоконфигурация его дала.</li>
 * </ul>
 * Это не отладочная игрушка: тот же отчёт можно проверять тестом, чтобы
 * зафиксировать поведение стартера — что при таких-то настройках бин появляется,
 * а при таких-то нет.
 */
public final class ConditionReport {

    private final ConfigurableApplicationContext context;

    public ConditionReport(ConfigurableApplicationContext context) {
        this.context = context;
    }

    private ConditionEvaluationReport evaluated() {
        return ConditionEvaluationReport.get(this.context.getBeanFactory());
    }

    /** Конфигурации, условия которых выполнились. */
    public List<String> included() {
        return evaluated().getConditionAndOutcomesBySource().entrySet().stream()
                .filter(entry -> entry.getValue().isFullMatch())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /** Конфигурации, условия которых не выполнились. */
    public List<String> excluded() {
        return evaluated().getConditionAndOutcomesBySource().entrySet().stream()
                .filter(entry -> !entry.getValue().isFullMatch())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /** Полный отчёт по всем конфигурациям, чьё имя содержит фрагмент. */
    public Map<String, ConditionEntry> matching(String nameFragment) {
        Map<String, ConditionEntry> result = new LinkedHashMap<>();
        evaluated().getConditionAndOutcomesBySource().forEach((source, outcomes) -> {
            if (!source.contains(nameFragment)) {
                return;
            }
            List<String> reasons = new java.util.ArrayList<>();
            outcomes.forEach(outcome -> reasons.add(
                    (outcome.getOutcome().isMatch() ? "✓ " : "✗ ") + outcome.getOutcome().getMessage()));
            result.put(source, new ConditionEntry(source, outcomes.isFullMatch(), reasons));
        });
        return result;
    }

    /** Почему конфигурация не применилась — дословная формулировка условия. */
    public Optional<String> whyExcluded(String nameFragment) {
        return matching(nameFragment).values().stream()
                .filter(entry -> !entry.matched())
                .flatMap(entry -> entry.reasons().stream())
                .filter(reason -> reason.startsWith("✗"))
                .findFirst();
    }

    /** Почему конфигурация применилась. */
    public Optional<String> whyIncluded(String nameFragment) {
        return matching(nameFragment).values().stream()
                .filter(ConditionEntry::matched)
                .flatMap(entry -> entry.reasons().stream())
                .findFirst();
    }

    /** Применилась ли указанная автоконфигурация. */
    public boolean applied(String nameFragment) {
        return matching(nameFragment).values().stream().anyMatch(ConditionEntry::matched);
    }

    /** Текстовый отчёт — то же, что печатает {@code --debug}, только по одной теме. */
    public String render(String nameFragment) {
        StringBuilder sb = new StringBuilder("Отчёт об условиях для '" + nameFragment + "':\n");
        Map<String, ConditionEntry> entries = matching(nameFragment);
        if (entries.isEmpty()) {
            return sb.append("  (нет подходящих конфигураций)").toString();
        }
        entries.forEach((source, entry) -> {
            sb.append("  ").append(entry.matched() ? "ПРИМЕНЕНА" : "ПРОПУЩЕНА")
                    .append(": ").append(source).append('\n');
            entry.reasons().forEach(reason -> sb.append("      ").append(reason).append('\n'));
        });
        return sb.toString();
    }
}

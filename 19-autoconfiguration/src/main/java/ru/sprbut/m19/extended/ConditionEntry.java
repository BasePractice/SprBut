/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19.extended;

import java.util.List;

/**
 * Строка отчёта об условиях: конфигурация, вердикт и его обоснование.
 *
 * <p>Обоснование хранится списком, а не строкой: условий на одной конфигурации
 * бывает несколько, и знать, какое именно не выполнилось, важнее, чем знать,
 * что «что-то не сошлось».</p>
 *
 * @param configuration имя конфигурации
 * @param matched       выполнились ли все её условия
 * @param reasons       формулировки условий с отметкой результата
 * @since 1.0
 */
public record ConditionEntry(String configuration, boolean matched, List<String> reasons) {

    /**
     * Значение {@code ConditionEntry}.
     */
    public ConditionEntry {
        reasons = List.copyOf(reasons);
    }
}

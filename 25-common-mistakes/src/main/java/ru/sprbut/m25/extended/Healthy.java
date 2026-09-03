/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m25.extended;

import ru.sprbut.m25.Diagnosis;

/**
 * Диагноз здоровой конфигурации: контекст поднялся, лечить нечего.
 * @since 1.0
 */
public final class Healthy implements Diagnosis {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Healthy() {
        // нечего инициализировать
    }

    @Override
    public String summary() {
        return "контекст поднялся без ошибок";
    }

    @Override
    public String remedy() {
        return "вмешательство не требуется";
    }
}

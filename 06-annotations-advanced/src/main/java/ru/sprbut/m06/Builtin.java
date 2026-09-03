/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06;

import java.lang.annotation.Annotation;

/**
 * Служебная ли это мета-аннотация самого языка.
 *
 * <p>Обход мета-аннотаций должен их исключать: {@code @Retention} помечена
 * {@code @Retention}, {@code @Target} — {@code @Target}, и отчёт утонул бы
 * в шуме ещё до того, как дошёл до прикладных аннотаций.</p>
 *
 * @since 1.0
 */
public final class Builtin {

    /**
     * Тип.
     */
    private final Class<? extends Annotation> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Builtin(final Class<? extends Annotation> type) {
        this.type = type;
    }

    /**
     * Принадлежит ли аннотация самому языку.
     * @return Принадлежит ли аннотация самому языку
     */
    public boolean yes() {
        return this.type.getName().startsWith("java.lang.annotation.")
            || this.type.getName().startsWith("jdk.internal.");
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Просит собрать для класса спутника с его паспортом — сразу байткодом,
 * минуя исходный текст.
 *
 * <p>Retention здесь такой же, как у остальных аннотаций модуля: в байткод
 * помеченного класса она не попадает, потому что вся её работа заканчивается
 * вместе с компиляцией.</p>
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Instrumented {
}

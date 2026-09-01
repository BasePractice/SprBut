/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m10.lombok.samples;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@code @RequiredArgsConstructor} — конструктор из {@code final} полей.
 *
 * <p>Это <b>основной способ</b> внедрения зависимостей через конструктор
 * в Spring-коде (модуль 12): полей два, конструктор пишется сам,
 * и добавление третьей зависимости не требует правки конструктора.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor
@Getter
public class Service {

    /**
     * Имя.
     */
    private final String name;

    /**
     * Число повторов.
     */
    private final int retries;

    /**
     * Состояние.
     */
    private String mutableState = "меняется";
}

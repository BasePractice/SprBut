/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.targets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 48: {@code TYPE_USE} — аннотация ставится не на объявление,
 * а на <b>использование типа</b>, включая аргументы дженериков.
 *
 * <p>Читается такая аннотация не с {@code Field}, а с {@code AnnotatedType} —
 * отдельной ветки API, о существовании которой обычно узнают,
 * когда {@code getAnnotations()} возвращает пустоту.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface NonNull {
}

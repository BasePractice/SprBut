/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает операцию, попадающую в журнал аудита.
 *
 * <p>{@code RUNTIME} здесь обязателен: аспект читает аннотацию рефлексией уже
 * во время работы приложения, и с {@code CLASS} или {@code SOURCE} метка
 * до него просто не доживёт.</p>
 *
 * <p>Сама по себе аннотация не делает ничего — это метаданные, а не поведение.
 * Работать её заставляет {@link AuditAspect}, ровно как {@code @Transactional}
 * работает благодаря прокси, а не собственной силе.</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * Имя операции в журнале; по умолчанию берётся имя метода.
     * @return Имя операции в журнале; по умолчанию берётся имя метода
     */
    String value() default "";
}

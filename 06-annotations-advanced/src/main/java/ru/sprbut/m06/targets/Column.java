/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 49: {@code RECORD_COMPONENT} — на компонент record (Java 16+).
 *
 * <p>Компонент record — не поле и не метод, а отдельная сущность, и аннотации
 * с него читаются своим API.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Column {

    /**
     * Имя колонки в таблице.
     * @return Имя колонки в таблице
     */
    String name();
}

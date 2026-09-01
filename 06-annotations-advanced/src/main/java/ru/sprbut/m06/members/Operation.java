/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.members;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайды 50–52: все допустимые типы элементов аннотации сразу.
 *
 * <p>Список закрыт: примитив, {@code String}, {@code Class}, enum, вложенная
 * аннотация и массивы из них — больше ничего положить нельзя. Причина в том,
 * что значения элементов обязаны быть константами времени компиляции,
 * иначе им негде было бы храниться в class-файле.</p>
 *
 * <p>Заодно видно, что {@code default} превращает элемент в необязательный:
 * здесь обязателен ровно один — {@code name}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Operation {

    /**
     * Имя операции — единственный обязательный элемент.
     * @return Имя операции — единственный обязательный элемент
     */
    String name();

    /**
     * Примитивный элемент.
     * @return Примитивный элемент
     */
    int timeout() default 30;

    /**
     * Ещё один примитив.
     * @return Ещё один примитив
     */
    boolean readOnly() default false;

    /**
     * Элемент типа {@code Class}.
     * @return Элемент типа {@code Class}
     */
    Class<? extends Throwable> rollbackFor() default RuntimeException.class;

    /**
     * Элемент-перечисление.
     * @return Элемент-перечисление
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Вложенная аннотация.
     * @return Вложенная аннотация
     */
    Retry retry() default @Retry;

    /**
     * Массив строк.
     * @return Массив строк
     */
    String[] tags() default {};

    /**
     * Массив классов.
     * @return Массив классов
     */
    Class<?>[] handles() default {};

    /**
     * Массив констант перечисления.
     * @return Массив констант перечисления
     */
    Isolation[] allowed() default {Isolation.DEFAULT};
}

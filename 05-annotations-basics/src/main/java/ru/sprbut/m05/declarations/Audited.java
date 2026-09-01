/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 41: {@code @Inherited}.
 *
 * <p>Аннотация с этой мета-аннотацией «спускается» на подклассы: если она стоит
 * на родителе, {@code subclass.getAnnotation(Audited.class)} её найдёт.</p>
 *
 * <p>Две ловушки, которые почти всегда становятся сюрпризом:
 * <ul>
 * <li>наследование работает <b>только по цепочке классов</b> — с интерфейса
 * на реализацию аннотация не переходит;</li>
 * <li>оно не распространяется на <b>методы и поля</b>: переопределённый метод
 * аннотацию родителя не наследует.</li>
 * </ul></p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Audited {

    /**
     * Значение {@code actor}.
     * @return Значение {@code actor}
     */
    String actor() default "system";
}

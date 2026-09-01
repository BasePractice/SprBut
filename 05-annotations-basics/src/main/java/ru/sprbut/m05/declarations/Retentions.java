/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 40: {@code @Retention{SOURCE, CLASS, RUNTIME}} — три аннотации, отличающиеся
 * только политикой хранения. На них видно, что политика решает всё.
 * <ul>
 * <li><b>SOURCE</b> — существует только для компилятора и annotation processor'ов;
 * в class-файл не попадает вовсе. Пример: {@code @Override}, лombok'овские аннотации;</li>
 * <li><b>CLASS</b> (значение по умолчанию!) — попадает в class-файл, но JVM её
 * не загружает. Читают только инструменты, работающие с байткодом;</li>
 * <li><b>RUNTIME</b> — доступна через рефлексию. Всё, на чём стоит Spring.</li>
 * </ul>
 * @since 1.0
 */
public final class Retentions {

    /**
     * Открытый конструктор: класс существует ради вложенных объявлений.
     */
    public Retentions() {
        // объявления аннотаций живут в теле класса, состояния у него нет
    }

    /**
     * Исчезает после компиляции.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public @interface SourceLevel {
    }

    /**
     * Есть в class-файле, но невидима для рефлексии. Это политика по умолчанию.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public @interface ClassLevel {
    }

    /**
     * Единственная политика, при которой аннотацию видно через рефлексию.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public @interface RuntimeLevel {
    }

    /**
     * Без {@code @Retention} действует политика по умолчанию — {@code CLASS}.
     * Самая частая причина «моя аннотация не читается»: её просто забыли пометить.
     */
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    public @interface DefaultRetention {
    }
}

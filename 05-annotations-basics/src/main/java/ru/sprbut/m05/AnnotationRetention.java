/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Слайд 40: {@code @Retention{SOURCE, CLASS, RUNTIME}}.
 *
 * <p>Единственный практический вывод: <b>если аннотация не помечена
 * {@code @Retention(RUNTIME)}, ни Spring, ни ваш собственный код её не увидят.</b>
 * Это причина примерно половины вопросов «почему моя аннотация не работает».</p>
 *
 * <p>Умолчание коварно: без {@code @Retention} политика равна {@code CLASS},
 * то есть аннотация попадёт в байткод, но останется невидимой для рефлексии.</p>
 *
 * @since 1.0
 */
public final class AnnotationRetention {

    /**
     * Аннотация.
     */
    private final Class<? extends Annotation> annotation;

    /**
     * Основной конструктор.
     * @param annotation Аннотация
     */
    public AnnotationRetention(final Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Политика хранения; {@code CLASS}, если {@code @Retention} не указан.
     * @return Политика хранения; {@code CLASS}, если {@code @Retention} не указан
     */
    public RetentionPolicy policy() {
        final Retention retention = this.annotation.getAnnotation(Retention.class);
        final RetentionPolicy policy;
        if (retention == null) {
            policy = RetentionPolicy.CLASS;
        } else {
            policy = retention.value();
        }
        return policy;
    }

    /**
     * Видна ли аннотация рефлексии.
     * @return Видна ли аннотация рефлексии
     */
    public boolean visible() {
        return this.policy() == RetentionPolicy.RUNTIME;
    }
}

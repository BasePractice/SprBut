/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.extended;

import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Поломка контекста, умеющая назвать себя.
 *
 * <p>Настоящая причина у Spring лежит не наверху: в вершине стека почти всегда
 * {@code UnsatisfiedDependencyException} со ссылкой на бин, а интересное —
 * этажами ниже. Поэтому разбор идёт по цепочке {@code getCause()} до первого
 * знакомого типа.</p>
 *
 * <p>Порядок проверок важен: {@code NoUniqueBeanDefinitionException} наследует
 * {@code NoSuchBeanDefinitionException}, и перепутанный порядок превратил бы
 * «бинов слишком много» в «бина нет».</p>
 *
 * @since 1.0
 */
public final class Failure implements Diagnosis {

    /**
     * Исключение, с которым упал старт контекста.
     */
    private final Throwable thrown;

    /**
     * Основной конструктор.
     * @param thrown Исключение, с которым упал старт
     */
    public Failure(final Throwable thrown) {
        this.thrown = thrown;
    }

    @Override
    public String summary() {
        return this.diagnosis().summary();
    }

    @Override
    public String remedy() {
        return this.diagnosis().remedy();
    }

    /**
     * Разбор цепочки причин до первой узнаваемой ошибки контейнера.
     * Цепочка {@code getCause()} у некоторых исключений замыкается на себя,
     * поэтому обход останавливается на первом же повторе.
     * @return Диагноз поломки
     */
    @SuppressWarnings("PMD.NullAssignment")
    public Diagnosis diagnosis() {
        Diagnosis found = null;
        Throwable cause = this.thrown;
        while (found == null && cause != null) {
            if (cause instanceof NoUniqueBeanDefinitionException unique) {
                found = new AmbiguousBean(unique);
            } else if (cause instanceof BeanCurrentlyInCreationException circular) {
                found = new CircularReference(circular);
            } else if (cause instanceof NoSuchBeanDefinitionException missing) {
                found = new MissingBean(missing);
            } else if (cause.equals(cause.getCause())) {
                cause = null;
            } else {
                cause = cause.getCause();
            }
        }
        if (found == null) {
            found = new UnknownFailure(this.thrown);
        }
        return found;
    }
}

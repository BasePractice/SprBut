/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Слайд 6: модификаторы одного члена класса.
 *
 * <p>{@code getModifiers()} возвращает не строку и не набор флагов, а обычный
 * {@code int}, в котором каждый бит означает один модификатор. {@link Modifier}
 * умеет его расшифровывать — и это единственная причина, по которой
 * «private final» получается прочитать словами.</p>
 *
 * @since 1.0
 */
public final class Modifiers {

    /**
     * Элемент класса.
     */
    private final Member member;

    /**
     * Основной конструктор.
     * @param member Элемент класса
     */
    public Modifiers(final Member member) {
        this.member = member;
    }

    /**
     * Человекочитаемое описание: {@code "private final"}.
     * @return Человекочитаемое описание: {@code "private final"}
     */
    public String text() {
        return Modifier.toString(this.member.getModifiers());
    }

    /**
     * Объявлен ли член финальным.
     * @return Объявлен ли член финальным
     */
    public boolean isFinal() {
        return Modifier.isFinal(this.member.getModifiers());
    }

    /**
     * Объявлен ли член статическим.
     * @return Объявлен ли член статическим
     */
    public boolean isStatic() {
        return Modifier.isStatic(this.member.getModifiers());
    }
}

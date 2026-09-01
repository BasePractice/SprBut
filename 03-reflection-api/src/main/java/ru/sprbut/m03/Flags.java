/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 25 (СХЕМА 1): {@link Modifier} — расшифровка битовой маски.
 *
 * <p>{@code getModifiers()} у {@code Class}, {@code Field}, {@code Method}
 * и {@code Constructor} возвращает один и тот же {@code int}. Это не enum
 * и не набор объектов — просто биты, поэтому проверки складываются побитовым «И».</p>
 *
 * <p>Package-private собственного бита не имеет: это отсутствие трёх остальных.
 * Ловушка, на которой спотыкается каждый второй сканер классов.</p>
 *
 * @since 1.0
 */
public final class Flags {

    /**
     * Маска.
     */
    private final int mask;

    /**
     * Основной конструктор.
     * @param member Элемент класса
     */
    public Flags(final Member member) {
        this(member.getModifiers());
    }

    /**
     * Основной конструктор.
     * @param mask Маска
     */
    public Flags(final int mask) {
        this.mask = mask;
    }

    /**
     * Все взведённые флаги списком имён.
     * @return Все взведённые флаги списком имён
     */
    public List<String> names() {
        final List<String> found = new ArrayList<>(0);
        if (Modifier.isPublic(this.mask)) {
            found.add("public");
        }
        if (Modifier.isProtected(this.mask)) {
            found.add("protected");
        }
        if (Modifier.isPrivate(this.mask)) {
            found.add("private");
        }
        if (Modifier.isStatic(this.mask)) {
            found.add("static");
        }
        if (Modifier.isFinal(this.mask)) {
            found.add("final");
        }
        if (Modifier.isAbstract(this.mask)) {
            found.add("abstract");
        }
        if (Modifier.isSynchronized(this.mask)) {
            found.add("synchronized");
        }
        if (Modifier.isVolatile(this.mask)) {
            found.add("volatile");
        }
        if (Modifier.isTransient(this.mask)) {
            found.add("transient");
        }
        if (Modifier.isNative(this.mask)) {
            found.add("native");
        }
        return List.copyOf(found);
    }

    /**
     * Ровно то, что печатает {@code javap}: модификаторы в каноническом порядке.
     * @return Ровно то, что печатает {@code javap}: модификаторы в каноническом порядке
     */
    public String text() {
        return Modifier.toString(this.mask);
    }

    /**
     * Package-private — отсутствие public, protected и private одновременно.
     * @return Package-private — отсутствие public, protected и private одновременно
     */
    public boolean packagePrivate() {
        return !Modifier.isPublic(this.mask)
            && !Modifier.isProtected(this.mask)
            && !Modifier.isPrivate(this.mask);
    }

    /**
     * Допустима ли эта маска для класса — {@code volatile}, например, нет.
     * @return Допустима ли эта маска для класса — {@code volatile}, например, нет
     */
    public boolean validForClass() {
        return (this.mask & Modifier.classModifiers()) == this.mask;
    }

    /**
     * Допустима ли эта маска для поля.
     * @return Допустима ли эта маска для поля
     */
    public boolean validForField() {
        return (this.mask & Modifier.fieldModifiers()) == this.mask;
    }
}

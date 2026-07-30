package ru.sprbut.m01;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Слайд 6: модификаторы одного члена класса.
 * <p>
 * {@code getModifiers()} возвращает не строку и не набор флагов, а обычный
 * {@code int}, в котором каждый бит означает один модификатор. {@link Modifier}
 * умеет его расшифровывать — и это единственная причина, по которой
 * «private final» получается прочитать словами.
 */
public final class Modifiers {

    private final Member member;

    public Modifiers(Member member) {
        this.member = member;
    }

    /**
     * Человекочитаемое описание: {@code "private final"}.
     */
    public String text() {
        return Modifier.toString(this.member.getModifiers());
    }

    /**
     * Объявлен ли член финальным.
     */
    public boolean isFinal() {
        return Modifier.isFinal(this.member.getModifiers());
    }

    /**
     * Объявлен ли член статическим.
     */
    public boolean isStatic() {
        return Modifier.isStatic(this.member.getModifiers());
    }
}

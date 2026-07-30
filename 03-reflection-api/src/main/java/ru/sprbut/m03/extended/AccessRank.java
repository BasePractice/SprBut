package ru.sprbut.m03.extended;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Насколько член класса доступен — числом, пригодным для сортировки.
 * <p>
 * Когда под аргументы подходят несколько конструкторов или методов, выбирать
 * приватный при наличии публичного неправильно: движок должен предпочитать то,
 * что автор класса объявил частью его интерфейса.
 */
public final class AccessRank {

    private final Member member;

    public AccessRank(Member member) {
        this.member = member;
    }

    /**
     * 0 для public, 1 для protected, 2 для package-private, 3 для private.
     */
    public int value() {
        int mask = this.member.getModifiers();
        if (Modifier.isPublic(mask)) {
            return 0;
        }
        if (Modifier.isProtected(mask)) {
            return 1;
        }
        if (Modifier.isPrivate(mask)) {
            return 3;
        }
        return 2;
    }
}

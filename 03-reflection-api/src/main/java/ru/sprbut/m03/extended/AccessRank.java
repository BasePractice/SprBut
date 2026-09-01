/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Насколько член класса доступен — числом, пригодным для сортировки.
 *
 * <p>Когда под аргументы подходят несколько конструкторов или методов, выбирать
 * приватный при наличии публичного неправильно: движок должен предпочитать то,
 * что автор класса объявил частью его интерфейса.</p>
 *
 * @since 1.0
 */
public final class AccessRank {

    /**
     * Элемент класса.
     */
    private final Member member;

    /**
     * Основной конструктор.
     * @param member Элемент класса
     */
    public AccessRank(final Member member) {
        this.member = member;
    }

    /**
     * 0 для public, 1 для protected, 2 для package-private, 3 для private.
     * @return 0 для public, 1 для protected, 2 для package-private, 3 для private
     */
    public int value() {
        final int mask = this.member.getModifiers();
        final int rank;
        if (Modifier.isPublic(mask)) {
            rank = 0;
        } else if (Modifier.isProtected(mask)) {
            rank = 1;
        } else if (Modifier.isPrivate(mask)) {
            rank = 3;
        } else {
            rank = 2;
        }
        return rank;
    }
}

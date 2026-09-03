/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m26.versions;

/**
 * Слайд «Версии»: правило переезда с Spring Boot 2 на 3.
 *
 * <p>Пакет {@code javax.*} стал {@code jakarta.*} — но не весь. Исключение,
 * на котором спотыкаются все: {@code javax.annotation.processing} остался
 * на месте, потому что принадлежит JDK, а не Java EE. Переехало то,
 * что Oracle передал фонду Eclipse.</p>
 *
 * @since 1.0
 */
public final class Migration {

    /**
     * Имя пакета до переезда.
     */
    private final String origin;

    /**
     * Основной конструктор.
     * @param origin Имя пакета до переезда
     */
    public Migration(final String origin) {
        this.origin = origin;
    }

    /**
     * Имя пакета после переезда на Jakarta EE 9.
     * @return Имя пакета после переезда
     */
    public String target() {
        final String target;
        if (this.origin.startsWith("javax.annotation.processing")
            || !this.origin.startsWith("javax.")) {
            target = this.origin;
        } else {
            target = this.origin.replaceFirst("^javax\\.", "jakarta.");
        }
        return target;
    }
}

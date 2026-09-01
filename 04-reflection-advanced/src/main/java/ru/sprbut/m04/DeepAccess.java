/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.Field;

/**
 * Слайд 31: «setAccessible и JPMS: нужен --add-opens».
 *
 * <p>Попытка снять проверку доступа с приватного поля чужого класса. Успех
 * зависит <b>не от модификатора поля</b>, а от того, открыт ли пакет —
 * и это самая неочевидная часть модульной системы.</p>
 *
 * <p>В этом проекте флаги заданы в {@code surefire.argLine} корневого pom.xml:
 * открыты {@code java.lang}, {@code java.util} и {@code java.time}.
 * Пакет {@code java.io} не открыт, и на нём разница видна.</p>
 *
 * @since 1.0
 */
public final class DeepAccess {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Поле.
     */
    private final String field;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param field Поле
     */
    public DeepAccess(final Class<?> type, final String field) {
        this.type = type;
        this.field = field;
    }

    /**
     * Результат попытки: успех либо имя и текст исключения.
     * @return Результат попытки: успех либо имя и текст исключения
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public AccessAttempt attempt() {
        try {
            final Field found = this.type.getDeclaredField(this.field);
            found.setAccessible(true);
            return AccessAttempt.ok();
        } catch (final NoSuchFieldException absent) {
            return new AccessAttempt(false, "NoSuchFieldException", absent.getMessage());
        } catch (final RuntimeException denied) {
            return new AccessAttempt(false, denied.getClass().getSimpleName(), denied.getMessage());
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Итог попытки открыть доступ к закрытому члену.
 * @param succeeded удалось ли снять проверку доступа
 * @param failure   имя класса исключения, если не удалось
 * @param message   сообщение исключения, если не удалось
 * @since 1.0
 */
public record AccessAttempt(boolean succeeded, String failure, String message) {

    /**
     * Успешная попытка.
     */
    public static AccessAttempt ok() {
        return new AccessAttempt(true, null, null);
    }
}

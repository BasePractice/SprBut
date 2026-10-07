/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.Map;
import java.util.UUID;

/**
 * Профиль пользователя.
 * @since 1.0
 */
public interface Profile {

    /**
     * Идентификатор, общий с учёткой в auth.
     * @return Идентификатор
     */
    UUID id();

    /**
     * Представление для ответа клиенту.
     * @return Поля по именам
     */
    Map<String, Object> json();

    /**
     * Сменить отображаемое имя.
     * @param name Новое имя
     */
    void rename(String name);

    /**
     * Удалить профиль.
     */
    void delete();
}

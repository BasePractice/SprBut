/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import ru.sprbut.m29.identity.Identity;

/**
 * Профили пользователей в сервисе profile.
 * @since 1.0
 */
@FunctionalInterface
public interface Profiles {

    /**
     * Завести профиль нового пользователя от его имени.
     * @param owner Новый пользователь
     * @param name  Отображаемое имя
     */
    void create(Identity owner, String name);
}

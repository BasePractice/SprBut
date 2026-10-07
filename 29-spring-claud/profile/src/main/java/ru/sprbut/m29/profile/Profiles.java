/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Все профили.
 * @since 1.0
 */
public interface Profiles {

    /**
     * Завести профиль.
     * @param id    Идентификатор учётки в auth
     * @param login Логин, копируемый из учётки
     * @param name  Отображаемое имя
     * @return Новый профиль
     */
    Profile add(UUID id, String login, String name);

    /**
     * Профиль по идентификатору.
     * @param id Идентификатор
     * @return Профиль, если он есть
     */
    Optional<Profile> profile(UUID id);

    /**
     * Все профили.
     * @return Профили
     */
    List<Profile> all();
}

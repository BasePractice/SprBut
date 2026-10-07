/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Характеристики пользователей, хранящиеся в сервисе параметров.
 * @since 1.0
 */
public interface Parameters {

    /**
     * Все характеристики пользователя.
     * @param owner Идентификатор пользователя
     * @return Значения по именам
     */
    Map<String, String> all(UUID owner);

    /**
     * Значение характеристики.
     * @param owner Идентификатор пользователя
     * @param name  Имя характеристики
     * @return Значение, если оно есть
     */
    Optional<String> value(UUID owner, String name);

    /**
     * Сохранить значение характеристики.
     * @param owner Идентификатор пользователя
     * @param name  Имя характеристики
     * @param value Значение
     */
    void put(UUID owner, String name, String value);

    /**
     * Удалить все характеристики пользователя.
     * @param owner Идентификатор пользователя
     */
    void delete(UUID owner);
}

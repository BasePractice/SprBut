/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.parameters;

import java.util.Map;
import java.util.Optional;

/**
 * Параметры, сгруппированные по владельцу и различаемые по имени.
 * @since 1.0
 */
public interface Parameters {

    /**
     * Все параметры владельца.
     * @param owner Владелец
     * @return Значения по именам
     */
    Map<String, String> all(String owner);

    /**
     * Значение параметра.
     * @param owner Владелец
     * @param name  Имя параметра
     * @return Значение, если параметр есть
     */
    Optional<String> value(String owner, String name);

    /**
     * Сохранить значение параметра, заменив прежнее.
     * @param owner Владелец
     * @param name  Имя параметра
     * @param value Значение
     */
    void put(String owner, String name, String value);

    /**
     * Удалить все параметры владельца.
     * @param owner Владелец
     */
    void delete(String owner);
}

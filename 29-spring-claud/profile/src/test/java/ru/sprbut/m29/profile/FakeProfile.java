/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Профиль в памяти для тестов, хранящий поля в общей таблице.
 * @since 1.0
 */
final class FakeProfile implements Profile {

    /**
     * Поля профилей по идентификаторам.
     */
    private final Map<UUID, Map<String, Object>> rows;

    /**
     * Идентификатор.
     */
    private final UUID uid;

    /**
     * Конструктор.
     * @param rows Поля профилей по идентификаторам
     * @param uid  Идентификатор
     */
    FakeProfile(final Map<UUID, Map<String, Object>> rows, final UUID uid) {
        this.rows = rows;
        this.uid = uid;
    }

    @Override
    public UUID id() {
        return this.uid;
    }

    @Override
    public Map<String, Object> json() {
        return new LinkedHashMap<>(this.rows.get(this.uid));
    }

    @Override
    public void rename(final String name) {
        this.rows.get(this.uid).put("name", name);
    }

    @Override
    public void delete() {
        this.rows.remove(this.uid);
    }
}

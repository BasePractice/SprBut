/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Учётка в памяти для тестов, хранящая поля в общей таблице.
 * @since 1.0
 */
final class FakeAccount implements Account {

    /**
     * Поля учёток по идентификаторам.
     */
    private final Map<UUID, Map<String, Object>> rows;

    /**
     * Идентификатор.
     */
    private final UUID uid;

    /**
     * Конструктор.
     * @param rows Поля учёток по идентификаторам
     * @param uid  Идентификатор
     */
    FakeAccount(final Map<UUID, Map<String, Object>> rows, final UUID uid) {
        this.rows = rows;
        this.uid = uid;
    }

    @Override
    public UUID id() {
        return this.uid;
    }

    @Override
    public String login() {
        return (String) this.rows.get(this.uid).get("login");
    }

    @Override
    public Role role() {
        return (Role) this.rows.get(this.uid).get("role");
    }

    @Override
    public Map<String, Object> json() {
        final Map<String, Object> json = new LinkedHashMap<>(this.rows.get(this.uid));
        json.remove("password");
        return json;
    }

    @Override
    public void password(final String password) {
        this.rows.get(this.uid).put("password", password);
    }

    @Override
    public void grant(final Role role) {
        this.rows.get(this.uid).put("role", role);
    }

    @Override
    public void delete() {
        this.rows.remove(this.uid);
    }
}

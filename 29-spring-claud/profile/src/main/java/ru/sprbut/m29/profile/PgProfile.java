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
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Профиль в таблице PostgreSQL, читаемый при каждом обращении.
 * @since 1.0
 */
public final class PgProfile implements Profile {

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Идентификатор.
     */
    private final UUID uid;

    /**
     * Конструктор.
     * @param jdbc Клиент базы
     * @param uid  Идентификатор
     */
    public PgProfile(final JdbcClient jdbc, final UUID uid) {
        this.jdbc = jdbc;
        this.uid = uid;
    }

    @Override
    public UUID id() {
        return this.uid;
    }

    @Override
    public Map<String, Object> json() {
        return this.jdbc.sql("SELECT id, login, name FROM profile WHERE id = :id")
            .param("id", this.uid)
            .query(
                (row, num) -> {
                    final Map<String, Object> json = new LinkedHashMap<>(3);
                    json.put("id", row.getObject("id", UUID.class));
                    json.put("login", row.getString("login"));
                    json.put("name", row.getString("name"));
                    return json;
                }
            )
            .single();
    }

    @Override
    public void rename(final String name) {
        final int rows = this.jdbc.sql("UPDATE profile SET name = :name WHERE id = :id")
            .params(Map.of("name", name, "id", this.uid))
            .update();
        if (rows != 1) {
            throw new IllegalStateException(
                String.format("Профиль %s не найден при переименовании", this.uid)
            );
        }
    }

    @Override
    public void delete() {
        this.jdbc.sql("DELETE FROM profile WHERE id = :id").param("id", this.uid).update();
    }
}

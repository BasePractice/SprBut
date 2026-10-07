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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Учётка в таблице PostgreSQL, читаемая при каждом обращении.
 * @since 1.0
 */
public final class PgAccount implements Account {

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Хеширование паролей.
     */
    private final PasswordEncoder encoder;

    /**
     * Идентификатор.
     */
    private final UUID uid;

    /**
     * Конструктор.
     * @param jdbc    Клиент базы
     * @param encoder Хеширование паролей
     * @param uid     Идентификатор
     */
    public PgAccount(final JdbcClient jdbc, final PasswordEncoder encoder, final UUID uid) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.uid = uid;
    }

    @Override
    public UUID id() {
        return this.uid;
    }

    @Override
    public String login() {
        return this.column("login");
    }

    @Override
    public Role role() {
        return Role.valueOf(this.column("role"));
    }

    @Override
    public Map<String, Object> json() {
        return this.jdbc.sql("SELECT id, login, role FROM account WHERE id = :id")
            .param("id", this.uid)
            .query(
                (row, num) -> {
                    final Map<String, Object> json = new LinkedHashMap<>(3);
                    json.put("id", row.getObject("id", UUID.class));
                    json.put("login", row.getString("login"));
                    json.put("role", row.getString("role"));
                    return json;
                }
            )
            .single();
    }

    @Override
    public void password(final String password) {
        this.change("password", this.encoder.encode(password));
    }

    @Override
    public void grant(final Role role) {
        this.change("role", role.name());
    }

    @Override
    public void delete() {
        this.jdbc.sql("DELETE FROM account WHERE id = :id").param("id", this.uid).update();
    }

    private String column(final String name) {
        return this.jdbc.sql(String.format("SELECT %s FROM account WHERE id = :id", name))
            .param("id", this.uid)
            .query(String.class)
            .single();
    }

    private void change(final String column, final String value) {
        final int rows = this.jdbc.sql(
            String.format("UPDATE account SET %s = :value WHERE id = :id", column)
        ).params(Map.of("value", value, "id", this.uid)).update();
        if (rows != 1) {
            throw new IllegalStateException(
                String.format("Учётка %s не найдена при смене поля %s", this.uid, column)
            );
        }
    }
}

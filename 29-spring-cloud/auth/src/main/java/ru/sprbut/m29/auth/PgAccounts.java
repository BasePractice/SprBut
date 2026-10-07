/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Учётки в таблице PostgreSQL.
 * @since 1.0
 */
@Component
public final class PgAccounts implements Accounts {

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Хеширование паролей.
     */
    private final PasswordEncoder encoder;

    /**
     * Конструктор.
     * @param jdbc    Клиент базы
     * @param encoder Хеширование паролей
     */
    public PgAccounts(final JdbcClient jdbc, final PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public Account add(final String login, final String password) {
        final UUID id = UUID.randomUUID();
        this.jdbc.sql(
            String.join(
                " ",
                "INSERT INTO account (id, login, password, role)",
                "VALUES (:id, :login, :password, :role)"
            )
        ).params(
            Map.of(
                "id", id,
                "login", login,
                "password", this.encoder.encode(password),
                "role", Role.USER.name()
            )
        ).update();
        return new PgAccount(this.jdbc, this.encoder, id);
    }

    @Override
    public Optional<Account> account(final UUID id) {
        return this.jdbc.sql("SELECT id FROM account WHERE id = :id")
            .param("id", id)
            .query(UUID.class)
            .optional()
            .map(found -> new PgAccount(this.jdbc, this.encoder, found));
    }

    @Override
    public Optional<Account> authenticated(final String login, final String password) {
        return this.jdbc.sql("SELECT id, password FROM account WHERE login = :login")
            .param("login", login)
            .query(
                (row, num) -> Map.entry(
                    row.getObject("id", UUID.class), row.getString("password")
                )
            )
            .optional()
            .filter(entry -> this.encoder.matches(password, entry.getValue()))
            .map(entry -> new PgAccount(this.jdbc, this.encoder, entry.getKey()));
    }
}

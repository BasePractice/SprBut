/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.dao.DuplicateKeyException;

/**
 * Учётки в памяти для тестов, пароли хранятся открытыми.
 * @since 1.0
 */
final class FakeAccounts implements Accounts {

    /**
     * Поля учёток по идентификаторам.
     */
    private final Map<UUID, Map<String, Object>> rows;

    /**
     * Конструктор без учёток.
     */
    FakeAccounts() {
        this(new ConcurrentHashMap<>(0));
    }

    /**
     * Конструктор.
     * @param rows Поля учёток по идентификаторам
     */
    FakeAccounts(final Map<UUID, Map<String, Object>> rows) {
        this.rows = rows;
    }

    @Override
    public Account add(final String login, final String password) {
        if (this.rows.values().stream().anyMatch(row -> login.equals(row.get("login")))) {
            throw new DuplicateKeyException(
                String.format("Логин '%s' уже занят в учётках в памяти", login)
            );
        }
        final UUID id = UUID.randomUUID();
        final Map<String, Object> row = new HashMap<>(4);
        row.put("id", id);
        row.put("login", login);
        row.put("password", password);
        row.put("role", Role.USER);
        this.rows.put(id, row);
        return new FakeAccount(this.rows, id);
    }

    @Override
    public Optional<Account> account(final UUID id) {
        return Optional.of(id).filter(this.rows::containsKey).map(
            found -> new FakeAccount(this.rows, found)
        );
    }

    @Override
    public Optional<Account> authenticated(final String login, final String password) {
        return this.rows.values().stream()
            .filter(row -> login.equals(row.get("login")) && password.equals(row.get("password")))
            .<Account>map(row -> new FakeAccount(this.rows, (UUID) row.get("id")))
            .findFirst();
    }
}

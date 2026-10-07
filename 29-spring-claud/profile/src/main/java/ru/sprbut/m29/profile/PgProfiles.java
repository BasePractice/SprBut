/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Профили в таблице PostgreSQL.
 * @since 1.0
 */
@Component
public final class PgProfiles implements Profiles {

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Конструктор.
     * @param jdbc Клиент базы
     */
    public PgProfiles(final JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Profile add(final UUID id, final String login, final String name) {
        this.jdbc.sql("INSERT INTO profile (id, login, name) VALUES (:id, :login, :name)")
            .params(Map.of("id", id, "login", login, "name", name))
            .update();
        return new PgProfile(this.jdbc, id);
    }

    @Override
    public Optional<Profile> profile(final UUID id) {
        return this.jdbc.sql("SELECT id FROM profile WHERE id = :id")
            .param("id", id)
            .query(UUID.class)
            .optional()
            .map(found -> new PgProfile(this.jdbc, found));
    }

    @Override
    public List<Profile> all() {
        return this.jdbc.sql("SELECT id FROM profile ORDER BY login")
            .query(UUID.class)
            .stream()
            .<Profile>map(id -> new PgProfile(this.jdbc, id))
            .toList();
    }
}

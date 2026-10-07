/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.parameters;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Параметры в таблице PostgreSQL.
 * @since 1.0
 */
@Component
public final class PgParameters implements Parameters {

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Конструктор.
     * @param jdbc Клиент базы
     */
    public PgParameters(final JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, String> all(final String owner) {
        return this.jdbc.sql("SELECT name, value FROM parameter WHERE owner = :owner")
            .param("owner", owner)
            .query((row, num) -> Map.entry(row.getString("name"), row.getString("value")))
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (left, right) -> right, TreeMap::new
                )
            );
    }

    @Override
    public Optional<String> value(final String owner, final String name) {
        return this.jdbc.sql("SELECT value FROM parameter WHERE owner = :owner AND name = :name")
            .param("owner", owner)
            .param("name", name)
            .query(String.class)
            .optional();
    }

    @Override
    public void put(final String owner, final String name, final String value) {
        this.jdbc.sql(
            String.join(
                " ",
                "INSERT INTO parameter (owner, name, value) VALUES (:owner, :name, :value)",
                "ON CONFLICT (owner, name) DO UPDATE SET value = EXCLUDED.value"
            )
        ).params(Map.of("owner", owner, "name", name, "value", value)).update();
    }

    @Override
    public void delete(final String owner) {
        this.jdbc.sql("DELETE FROM parameter WHERE owner = :owner").param("owner", owner).update();
    }
}

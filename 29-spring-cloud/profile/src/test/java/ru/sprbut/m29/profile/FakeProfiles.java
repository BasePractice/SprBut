/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.dao.DuplicateKeyException;

/**
 * Профили в памяти для тестов.
 * @since 1.0
 */
final class FakeProfiles implements Profiles {

    /**
     * Поля профилей по идентификаторам.
     */
    private final Map<UUID, Map<String, Object>> rows;

    /**
     * Конструктор без профилей.
     */
    FakeProfiles() {
        this(new ConcurrentHashMap<>(0));
    }

    /**
     * Конструктор.
     * @param rows Поля профилей по идентификаторам
     */
    FakeProfiles(final Map<UUID, Map<String, Object>> rows) {
        this.rows = rows;
    }

    @Override
    public Profile add(final UUID id, final String login, final String name) {
        if (this.rows.containsKey(id)
            || this.rows.values().stream().anyMatch(row -> login.equals(row.get("login")))) {
            throw new DuplicateKeyException(
                String.format("Профиль %s или логин '%s' уже есть в фейке", id, login)
            );
        }
        final Map<String, Object> row = new HashMap<>(3);
        row.put("id", id);
        row.put("login", login);
        row.put("name", name);
        this.rows.put(id, row);
        return new FakeProfile(this.rows, id);
    }

    @Override
    public Optional<Profile> profile(final UUID id) {
        return Optional.of(id).filter(this.rows::containsKey).map(
            found -> new FakeProfile(this.rows, found)
        );
    }

    @Override
    public List<Profile> all() {
        return this.rows.keySet().stream().<Profile>map(id -> new FakeProfile(this.rows, id))
            .toList();
    }
}

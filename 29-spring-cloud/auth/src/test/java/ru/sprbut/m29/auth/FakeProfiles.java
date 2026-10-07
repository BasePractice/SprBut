/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.sprbut.m29.identity.Identity;

/**
 * Профили в памяти для тестов: имя по полям пользователя.
 * @since 1.0
 */
final class FakeProfiles implements Profiles {

    /**
     * Имена по идентификаторам.
     */
    private final Map<Object, String> names;

    /**
     * Конструктор пустых профилей.
     */
    FakeProfiles() {
        this(new ConcurrentHashMap<>(0));
    }

    /**
     * Конструктор.
     * @param names Имена по идентификаторам
     */
    FakeProfiles(final Map<Object, String> names) {
        this.names = names;
    }

    @Override
    public void create(final Identity owner, final String name) {
        this.names.put(owner.json().get("id"), name);
    }

    /**
     * Имя в профиле пользователя.
     * @param id Идентификатор
     * @return Имя или пустая строка
     */
    String name(final Object id) {
        return this.names.getOrDefault(id.toString(), "");
    }
}

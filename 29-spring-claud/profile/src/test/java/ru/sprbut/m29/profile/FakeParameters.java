/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Характеристики в памяти для тестов.
 * @since 1.0
 */
final class FakeParameters implements Parameters {

    /**
     * Значения по владельцам и именам.
     */
    private final Map<UUID, Map<String, String>> map;

    /**
     * Конструктор пустых характеристик.
     */
    FakeParameters() {
        this(new ConcurrentHashMap<>(0));
    }

    /**
     * Конструктор.
     * @param map Значения по владельцам и именам
     */
    FakeParameters(final Map<UUID, Map<String, String>> map) {
        this.map = map;
    }

    @Override
    public Map<String, String> all(final UUID owner) {
        return new TreeMap<>(this.map.getOrDefault(owner, Map.of()));
    }

    @Override
    public Optional<String> value(final UUID owner, final String name) {
        return Optional.ofNullable(this.map.getOrDefault(owner, Map.of()).get(name));
    }

    @Override
    public void put(final UUID owner, final String name, final String value) {
        this.map.computeIfAbsent(owner, key -> new ConcurrentHashMap<>(1)).put(name, value);
    }

    @Override
    public void delete(final UUID owner) {
        this.map.remove(owner);
    }
}

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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Параметры в памяти для тестов.
 * @since 1.0
 */
final class FakeParameters implements Parameters {

    /**
     * Значения по владельцам и именам.
     */
    private final Map<String, Map<String, String>> map;

    /**
     * Конструктор пустых параметров.
     */
    FakeParameters() {
        this(new ConcurrentHashMap<>(0));
    }

    /**
     * Конструктор.
     * @param map Значения по владельцам и именам
     */
    FakeParameters(final Map<String, Map<String, String>> map) {
        this.map = map;
    }

    @Override
    public Map<String, String> all(final String owner) {
        return new TreeMap<>(this.map.getOrDefault(owner, Map.of()));
    }

    @Override
    public Optional<String> value(final String owner, final String name) {
        return Optional.ofNullable(this.map.getOrDefault(owner, Map.of()).get(name));
    }

    @Override
    public void put(final String owner, final String name, final String value) {
        this.map.computeIfAbsent(owner, key -> new ConcurrentHashMap<>(1)).put(name, value);
    }

    @Override
    public void delete(final String owner) {
        this.map.remove(owner);
    }
}

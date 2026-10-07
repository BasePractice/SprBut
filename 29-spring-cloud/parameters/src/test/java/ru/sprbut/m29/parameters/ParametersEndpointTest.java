/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.parameters;

import java.util.Map;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Тесты {@link ParametersEndpoint}.
 * @since 1.0
 */
final class ParametersEndpointTest {

    @Test
    @DisplayName("endpoint возвращает сохранённое значение")
    void returnsStoredValue() {
        final ParametersEndpoint endpoint = new ParametersEndpoint(new FakeParameters());
        final String value = UUID.randomUUID().toString();
        endpoint.put("owner", "color", value);
        MatcherAssert.assertThat(
            "endpoint не вернул сохранённое значение",
            endpoint.value("owner", "color"),
            Matchers.equalTo(value)
        );
    }

    @Test
    @DisplayName("повторная запись заменяет значение")
    void replacesValueOnSecondPut() {
        final ParametersEndpoint endpoint = new ParametersEndpoint(new FakeParameters());
        final String value = UUID.randomUUID().toString();
        endpoint.put("tenant", "size", "первое");
        endpoint.put("tenant", "size", value);
        MatcherAssert.assertThat(
            "endpoint оставил старое значение после замены",
            endpoint.value("tenant", "size"),
            Matchers.equalTo(value)
        );
    }

    @Test
    @DisplayName("на отсутствующую характеристику endpoint отвечает 404")
    void rejectsMissingParameter() {
        MatcherAssert.assertThat(
            "endpoint не ответил 404 на отсутствующую характеристику",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new ParametersEndpoint(new FakeParameters()).value("ghost", "zzz")
            ).getStatusCode().value(),
            Matchers.equalTo(404)
        );
    }

    @Test
    @DisplayName("endpoint отдаёт характеристики только своего владельца")
    void returnsOnlyParametersOfOwner() {
        final ParametersEndpoint endpoint = new ParametersEndpoint(new FakeParameters());
        endpoint.put("alice", "city", "Тверь");
        endpoint.put("bob", "city", "Омск");
        MatcherAssert.assertThat(
            "endpoint смешал характеристики разных владельцев",
            endpoint.all("alice"),
            Matchers.equalTo(Map.of("city", "Тверь"))
        );
    }

    @Test
    @DisplayName("удаление владельца стирает все его характеристики")
    void deletesAllParametersOfOwner() {
        final ParametersEndpoint endpoint = new ParametersEndpoint(new FakeParameters());
        endpoint.put("gone", "city", "Псков");
        endpoint.put("gone", "pet", UUID.randomUUID().toString());
        endpoint.delete("gone");
        MatcherAssert.assertThat(
            "endpoint оставил характеристики удалённого владельца",
            endpoint.all("gone"),
            Matchers.anEmptyMap()
        );
    }

    @Test
    @DisplayName("удаление владельца не трогает характеристики других")
    void keepsParametersOfOtherOwnersOnDelete() {
        final ParametersEndpoint endpoint = new ParametersEndpoint(new FakeParameters());
        final String value = UUID.randomUUID().toString();
        endpoint.put("stay", "pet", value);
        endpoint.put("leave", "pet", "кот");
        endpoint.delete("leave");
        MatcherAssert.assertThat(
            "endpoint удалил характеристики другого владельца",
            endpoint.value("stay", "pet"),
            Matchers.equalTo(value)
        );
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Характеристики в удалённом сервисе параметров.
 *
 * <p>Адрес сервиса клиент получает из реестра Eureka по имени сервиса,
 * если его построитель помечен как балансируемый.</p>
 *
 * @since 1.0
 */
public final class RemoteParameters implements Parameters {

    /**
     * HTTP-клиент с базовым адресом сервиса параметров.
     */
    private final RestClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса параметров
     */
    public RemoteParameters(final RestClient client) {
        this.client = client;
    }

    @Override
    public Map<String, String> all(final UUID owner) {
        return Objects.requireNonNull(
            this.client.get()
                .uri("/parameters/{owner}", owner)
                .retrieve().body(
                    new ParameterizedTypeReference<Map<String, String>>() {
                    }
                ),
            String.format("Сервис параметров вернул пустой ответ для пользователя %s", owner)
        );
    }

    @Override
    public Optional<String> value(final UUID owner, final String name) {
        final ResponseEntity<String> entity = this.client.get()
            .uri("/parameters/{owner}/{name}", owner, name)
            .accept(MediaType.TEXT_PLAIN)
            .retrieve().onStatus(
                status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
                (req, res) -> {
                }
            )
            .toEntity(String.class);
        final Optional<String> value;
        if (entity.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            value = Optional.empty();
        } else {
            value = Optional.of(Objects.requireNonNullElse(entity.getBody(), ""));
        }
        return value;
    }

    @Override
    public void put(final UUID owner, final String name, final String value) {
        this.client.put()
            .uri("/parameters/{owner}/{name}", owner, name)
            .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
            .body(value)
            .retrieve()
            .toBodilessEntity();
    }

    @Override
    public void delete(final UUID owner) {
        this.client.delete().uri("/parameters/{owner}", owner).retrieve().toBodilessEntity();
    }
}

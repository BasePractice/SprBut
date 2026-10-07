/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Характеристики в удалённом сервисе parameters.
 * @since 1.0
 */
public final class RemoteParameters implements Parameters {

    /**
     * HTTP-клиент с базовым адресом сервиса parameters.
     */
    private final WebClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса parameters
     */
    public RemoteParameters(final WebClient client) {
        this.client = client;
    }

    @Override
    public Mono<Map<String, String>> all(final String owner) {
        return this.client.get()
            .uri("/parameters/{owner}", owner)
            .retrieve().bodyToMono(
                new ParameterizedTypeReference<Map<String, String>>() {
                }
            );
    }
}

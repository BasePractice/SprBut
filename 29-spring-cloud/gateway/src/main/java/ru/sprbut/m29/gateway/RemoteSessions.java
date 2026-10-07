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
 * Сессии, о которых спрашивают сервис auth.
 * @since 1.0
 */
public final class RemoteSessions implements Sessions {

    /**
     * HTTP-клиент с базовым адресом сервиса auth.
     */
    private final WebClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса auth
     */
    public RemoteSessions(final WebClient client) {
        this.client = client;
    }

    @Override
    public Mono<Boolean> active(final String sid) {
        return this.client.get()
            .uri("/internal/sessions/{sid}", sid)
            .retrieve().bodyToMono(
                new ParameterizedTypeReference<Map<String, Boolean>>() {
                }
            )
            .map(json -> Boolean.TRUE.equals(json.get("active")));
    }
}

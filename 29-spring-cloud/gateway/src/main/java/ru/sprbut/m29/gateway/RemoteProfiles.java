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
import ru.sprbut.m29.identity.Identity;

/**
 * Профили в удалённом сервисе profile.
 * @since 1.0
 */
public final class RemoteProfiles implements Profiles {

    /**
     * HTTP-клиент с базовым адресом сервиса profile.
     */
    private final WebClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса profile
     */
    public RemoteProfiles(final WebClient client) {
        this.client = client;
    }

    @Override
    public Mono<Map<String, Object>> profile(final Identity owner) {
        return this.client.get()
            .uri("/profiles/{id}", owner.json().get("id"))
            .headers(headers -> owner.headers().forEach(headers::set))
            .retrieve().bodyToMono(
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
            );
    }
}

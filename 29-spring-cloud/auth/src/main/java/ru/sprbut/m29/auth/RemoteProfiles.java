/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import ru.sprbut.m29.identity.Identity;

/**
 * Профили в удалённом сервисе profile, адрес которого берётся из Eureka.
 * @since 1.0
 */
public final class RemoteProfiles implements Profiles {

    /**
     * HTTP-клиент с базовым адресом сервиса profile.
     */
    private final RestClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса profile
     */
    public RemoteProfiles(final RestClient client) {
        this.client = client;
    }

    @Override
    public void create(final Identity owner, final String name) {
        final Map<String, Object> body = new HashMap<>(owner.json());
        body.remove("role");
        body.put("name", name);
        this.client.post()
            .uri("/profiles")
            .headers(headers -> owner.headers().forEach(headers::add))
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }
}

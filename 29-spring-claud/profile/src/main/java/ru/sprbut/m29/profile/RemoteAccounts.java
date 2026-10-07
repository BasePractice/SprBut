/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import org.springframework.web.client.RestClient;
import ru.sprbut.m29.identity.Identity;

/**
 * Учётки в удалённом сервисе auth, адрес которого берётся из Eureka.
 * @since 1.0
 */
public final class RemoteAccounts implements Accounts {

    /**
     * HTTP-клиент с базовым адресом сервиса auth.
     */
    private final RestClient client;

    /**
     * Конструктор.
     * @param client HTTP-клиент с базовым адресом сервиса auth
     */
    public RemoteAccounts(final RestClient client) {
        this.client = client;
    }

    @Override
    public void delete(final Identity caller, final UUID id) {
        this.client.delete()
            .uri("/auth/users/{id}", id)
            .headers(headers -> caller.headers().forEach(headers::add))
            .retrieve()
            .toBodilessEntity();
    }
}

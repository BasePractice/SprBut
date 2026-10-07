/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import org.springframework.web.client.RestClientException;
import ru.sprbut.m29.identity.Identity;

/**
 * Недоступный сервис профилей для тестов.
 * @since 1.0
 */
final class BrokenProfiles implements Profiles {

    @Override
    public void create(final Identity owner, final String name) {
        throw new RestClientException(
            String.format(
                "Сервис профилей недоступен для пользователя %s", owner.json().get("login")
            )
        );
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.sprbut.m29.identity.Identity;

/**
 * Метод {@code /me}: профиль и характеристики текущего пользователя,
 * запрошенные параллельно.
 * @since 1.0
 */
@RestController
public final class MeEndpoint {

    /**
     * Профили.
     */
    private final Profiles profiles;

    /**
     * Характеристики.
     */
    private final Parameters parameters;

    /**
     * Конструктор.
     * @param profiles   Профили
     * @param parameters Характеристики
     */
    public MeEndpoint(final Profiles profiles, final Parameters parameters) {
        this.profiles = profiles;
        this.parameters = parameters;
    }

    /**
     * Текущий пользователь: профиль с ролью из токена и характеристики.
     * @param jwt Проверенный access-токен
     * @return Поля {@code profile} и {@code parameters}
     */
    @GetMapping("/me")
    public Mono<Map<String, Object>> current(@AuthenticationPrincipal final Jwt jwt) {
        final Identity identity = new Bearer(jwt).identity();
        return Mono.zip(
            this.profiles.profile(identity),
            this.parameters.all(jwt.getSubject())
        ).map(
            pair -> {
                final Map<String, Object> profile = new LinkedHashMap<>(pair.getT1());
                profile.put("role", identity.json().get("role"));
                return Map.<String, Object>of("profile", profile, "parameters", pair.getT2());
            }
        ).onErrorMap(
            WebClientException.class,
            ex -> new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                String.format(
                    "Профиль или характеристики пользователя %s недоступны", jwt.getSubject()
                ),
                ex
            )
        );
    }
}

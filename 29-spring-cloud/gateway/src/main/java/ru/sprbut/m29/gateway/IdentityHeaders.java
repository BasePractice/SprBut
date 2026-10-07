/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.sprbut.m29.identity.Header;

/**
 * Снимает с запроса токен и пришедшие снаружи X-User-*, ставит свои
 * X-User-* по проверенному токену.
 *
 * <p>Сервисы за gateway токен не проверяют и верят этим заголовкам.</p>
 *
 * @since 1.0
 */
public final class IdentityHeaders implements GlobalFilter, Ordered {

    /**
     * Конструктор.
     */
    public IdentityHeaders() {
        // Состояния у фильтра нет.
    }

    @Override
    public Mono<Void> filter(
        final ServerWebExchange exchange,
        final @NonNull GatewayFilterChain chain
    ) {
        return exchange.getPrincipal()
            .ofType(JwtAuthenticationToken.class)
            .map(token -> new Bearer(token.getToken()).identity().headers())
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty())
            .flatMap(
                identity -> chain.filter(
                    exchange.mutate().request(
                        request -> request.headers(
                            headers -> IdentityHeaders.replace(headers, identity)
                        )
                    ).build()
                )
            );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static void replace(
        final HttpHeaders headers,
        final Optional<Map<String, String>> identity
    ) {
        headers.remove(HttpHeaders.AUTHORIZATION);
        for (final Header header : Header.values()) {
            headers.remove(header.toString());
        }
        identity.ifPresent(values -> values.forEach(headers::set));
    }
}

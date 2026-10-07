/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.time.Duration;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.sprbut.m29.identity.Identity;

/**
 * Тесты {@link TokensEndpoint}.
 * @since 1.0
 */
final class TokensEndpointTest {

    @Test
    @DisplayName("токен доступа выдаётся с учёткой в качестве субъекта")
    void issuesAccessTokenWithAccountAsSubject() throws Exception {
        final Accounts accounts = new FakeAccounts();
        final FreshKey key = new FreshKey();
        accounts.add("nina", "п4роль");
        MatcherAssert.assertThat(
            "субъект токена доступа не совпал с вошедшей учёткой",
            key.decoder().decode(
                (String) new TokensEndpoint(
                    accounts, new FakeSessions(), key.token(Duration.ofMinutes(9))
                ).login(new TokensEndpoint.Credentials("nina", "п4роль")).get("access_token")
            ).getSubject(),
            Matchers.equalTo(
                accounts.authenticated("nina", "п4роль").orElseThrow().id().toString()
            )
        );
    }

    @Test
    @DisplayName("токен доступа привязан к активной сессии")
    void bindsAccessTokenToActiveSession() throws Exception {
        final Accounts accounts = new FakeAccounts();
        final Sessions sessions = new FakeSessions();
        final FreshKey key = new FreshKey();
        accounts.add("oleg", "pwd");
        MatcherAssert.assertThat(
            "токен доступа не указывает на активную сессию",
            sessions.active(
                UUID.fromString(
                    key.decoder().decode(
                        (String) new TokensEndpoint(
                            accounts, sessions, key.token(Duration.ofHours(1))
                        ).login(new TokensEndpoint.Credentials("oleg", "pwd")).get("access_token")
                    ).getClaimAsString("sid")
                )
            ),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("неверный пароль отвергается кодом 401")
    void rejectsWrongPassword() throws Exception {
        final Accounts accounts = new FakeAccounts();
        accounts.add("egor", "верный");
        MatcherAssert.assertThat(
            "неверный пароль не дал кода 401",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new TokensEndpoint(
                    accounts, new FakeSessions(), new FreshKey().token(Duration.ofMinutes(3))
                ).login(new TokensEndpoint.Credentials("egor", "неверный"))
            ).getStatusCode().value(),
            Matchers.equalTo(401)
        );
    }

    @Test
    @DisplayName("обмен выдаёт новый токен обновления")
    void rotatesRefreshToken() throws Exception {
        final Accounts accounts = new FakeAccounts();
        accounts.add("rita", "pwd");
        final TokensEndpoint endpoint = new TokensEndpoint(
            accounts, new FakeSessions(), new FreshKey().token(Duration.ofMinutes(5))
        );
        final String first = (String) endpoint.login(new TokensEndpoint.Credentials("rita", "pwd"))
            .get("refresh_token");
        MatcherAssert.assertThat(
            "обмен вернул тот же токен обновления",
            endpoint.refresh(new TokensEndpoint.Exchange(first)).get("refresh_token"),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    @Test
    @DisplayName("повторное использование токена обновления закрывает сессию")
    void closesSessionWhenRefreshTokenIsReused() throws Exception {
        final Accounts accounts = new FakeAccounts();
        accounts.add("igor", "pwd");
        final TokensEndpoint endpoint = new TokensEndpoint(
            accounts, new FakeSessions(), new FreshKey().token(Duration.ofMinutes(5))
        );
        final String first = (String) endpoint.login(new TokensEndpoint.Credentials("igor", "pwd"))
            .get("refresh_token");
        final String second = (String) endpoint.refresh(new TokensEndpoint.Exchange(first))
            .get("refresh_token");
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> endpoint.refresh(new TokensEndpoint.Exchange(first))
        );
        MatcherAssert.assertThat(
            "свежий токен обновления пережил повторное использование старого",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> endpoint.refresh(new TokensEndpoint.Exchange(second))
            ).getStatusCode().value(),
            Matchers.equalTo(401)
        );
    }

    @Test
    @DisplayName("выход закрывает сессию")
    void closesSessionOnLogout() throws Exception {
        final Accounts accounts = new FakeAccounts();
        final UUID id = accounts.add("vlad", "pwd").id();
        final TokensEndpoint endpoint = new TokensEndpoint(
            accounts, new FakeSessions(), new FreshKey().token(Duration.ofMinutes(5))
        );
        final String refresh = (String) endpoint.login(
            new TokensEndpoint.Credentials("vlad", "pwd")
        ).get("refresh_token");
        endpoint.logout(
            new TokensEndpoint.Exchange(refresh),
            new Identity(id.toString(), "USER", "vlad").authentication()
        );
        MatcherAssert.assertThat(
            "токен обновления работает и после выхода",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> endpoint.refresh(new TokensEndpoint.Exchange(refresh))
            ).getStatusCode().value(),
            Matchers.equalTo(401)
        );
    }
}

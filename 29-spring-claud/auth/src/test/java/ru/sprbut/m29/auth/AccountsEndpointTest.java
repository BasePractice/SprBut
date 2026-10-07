/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Тесты {@link AccountsEndpoint}.
 * @since 1.0
 */
final class AccountsEndpointTest {

    @Test
    @DisplayName("смена пароля закрывает сессии учётки")
    void closesSessionsOnPasswordChange() {
        final Accounts accounts = new FakeAccounts();
        final Sessions sessions = new FakeSessions();
        final UUID id = accounts.add("lena", "old").id();
        final Ticket ticket = sessions.open(id);
        new AccountsEndpoint(accounts, sessions).password(
            id, new AccountsEndpoint.Secret(UUID.randomUUID().toString())
        );
        MatcherAssert.assertThat(
            "сессия пережила смену пароля",
            sessions.active(ticket.session()),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("смена роли закрывает сессии со старой ролью")
    void closesSessionsOnRoleChange() {
        final Accounts accounts = new FakeAccounts();
        final Sessions sessions = new FakeSessions();
        final UUID id = accounts.add("gena", "pwd").id();
        final Ticket ticket = sessions.open(id);
        new AccountsEndpoint(accounts, sessions).grant(id, new AccountsEndpoint.Grant(Role.ADMIN));
        MatcherAssert.assertThat(
            "сессия со старой ролью пережила смену роли",
            sessions.active(ticket.session()),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("выдача роли меняет роль учётки")
    void grantsRole() {
        final Accounts accounts = new FakeAccounts();
        MatcherAssert.assertThat(
            "выдача роли не изменила роль",
            new AccountsEndpoint(accounts, new FakeSessions()).grant(
                accounts.add("sasha", "pwd").id(), new AccountsEndpoint.Grant(Role.ADMIN)
            ),
            Matchers.hasEntry("role", Role.ADMIN)
        );
    }

    @Test
    @DisplayName("отзыв закрывает все сессии учётки")
    void revokesAllSessions() {
        final Accounts accounts = new FakeAccounts();
        final Sessions sessions = new FakeSessions();
        final UUID id = accounts.add("dima", "pwd").id();
        sessions.open(id);
        sessions.open(id);
        new AccountsEndpoint(accounts, sessions).revoke(id);
        MatcherAssert.assertThat(
            "часть сессий пережила отзыв",
            new AccountsEndpoint(accounts, sessions).sessions(id),
            Matchers.empty()
        );
    }

    @Test
    @DisplayName("повторное удаление учётки проходит без ошибки")
    void deletesAccountTwiceWithoutComplaint() {
        final Accounts accounts = new FakeAccounts();
        final UUID id = accounts.add("tolya", "pwd").id();
        final AccountsEndpoint endpoint = new AccountsEndpoint(accounts, new FakeSessions());
        endpoint.delete(id);
        endpoint.delete(id);
        MatcherAssert.assertThat(
            "удалённая учётка всё ещё на месте",
            accounts.account(id).isPresent(),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("сессии несуществующей учётки отвечают кодом 404")
    void rejectsSessionsOfMissingAccount() {
        MatcherAssert.assertThat(
            "сессии несуществующей учётки не ответили кодом 404",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new AccountsEndpoint(new FakeAccounts(), new FakeSessions())
                    .sessions(UUID.randomUUID())
            ).getStatusCode().value(),
            Matchers.equalTo(404)
        );
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Вход, обмен refresh-токена на новую пару и выход.
 * @since 1.0
 */
@RestController
@RequestMapping("/auth")
public final class TokensEndpoint {

    /**
     * Учётки.
     */
    private final Accounts accounts;

    /**
     * Сессии.
     */
    private final Sessions sessions;

    /**
     * Выпуск access-токенов.
     */
    private final Token token;

    /**
     * Конструктор.
     * @param accounts Учётки
     * @param sessions Сессии
     * @param token    Выпуск access-токенов
     */
    public TokensEndpoint(final Accounts accounts, final Sessions sessions, final Token token) {
        this.accounts = accounts;
        this.sessions = sessions;
        this.token = token;
    }

    /**
     * Войти: открыть сессию и выдать пару токенов.
     * @param form Логин и пароль
     * @return Пара токенов
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody final Credentials form) {
        final Account account = this.accounts.authenticated(form.login(), form.password())
            .orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    String.format("Логин '%s' или пароль неверны", form.login())
                )
            );
        return this.pair(account, this.sessions.open(account.id()));
    }

    /**
     * Обменять refresh-токен на новую пару.
     * @param form Refresh-токен
     * @return Пара токенов
     */
    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody final Exchange form) {
        final Ticket ticket = this.sessions.rotate(form.token()).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Токен обновления неизвестен, просрочен или уже использован"
            )
        );
        return this.pair(
            this.accounts.account(ticket.account()).orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    String.format("Учётка %s этой сессии удалена", ticket.account())
                )
            ),
            ticket
        );
    }

    /**
     * Выйти: закрыть сессию refresh-токена вместе с её access-токенами.
     * @param form   Refresh-токен
     * @param caller Пользователь из заголовков gateway
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody final Exchange form, final Authentication caller) {
        this.sessions.close(UUID.fromString(caller.getName()), form.token());
    }

    private Map<String, Object> pair(final Account account, final Ticket ticket) {
        final Map<String, Object> pair = new LinkedHashMap<>(4);
        pair.put("access_token", this.token.issued(account, ticket.session()));
        pair.put("refresh_token", ticket.refresh());
        pair.put("token_type", "Bearer");
        pair.put("expires_in", this.token.seconds());
        return pair;
    }

    /**
     * Логин и пароль.
     * @param login    Логин
     * @param password Пароль
     * @since 1.0
     */
    public record Credentials(String login, String password) {
        /**
         * Конструктор, отвергающий пропущенные поля.
         * @param login    Логин
         * @param password Пароль
         */
        public Credentials {
            Objects.requireNonNull(login, "Для входа нужен логин");
            Objects.requireNonNull(password, "Для входа нужен пароль");
        }
    }

    /**
     * Refresh-токен для обмена или выхода.
     * @param token Refresh-токен
     * @since 1.0
     */
    public record Exchange(@JsonProperty("refresh_token") String token) {
        /**
         * Конструктор, отвергающий пропущенный токен.
         * @param token Refresh-токен
         */
        public Exchange {
            Objects.requireNonNull(token, "Поле refresh_token обязательно");
        }
    }
}

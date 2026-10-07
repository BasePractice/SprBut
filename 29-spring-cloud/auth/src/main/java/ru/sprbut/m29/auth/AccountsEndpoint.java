/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Управление учёткой: сессии, отзыв, пароль, роль и удаление.
 *
 * <p>Смена пароля или роли закрывает все сессии: токены со старой
 * ролью не должны жить дальше.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/auth/users/{id}")
public final class AccountsEndpoint {

    /**
     * Учётки.
     */
    private final Accounts accounts;

    /**
     * Сессии.
     */
    private final Sessions sessions;

    /**
     * Конструктор.
     * @param accounts Учётки
     * @param sessions Сессии
     */
    public AccountsEndpoint(final Accounts accounts, final Sessions sessions) {
        this.accounts = accounts;
        this.sessions = sessions;
    }

    /**
     * Активные сессии учётки.
     * @param id Идентификатор учётки
     * @return Сессии без токенов
     */
    @GetMapping("/sessions")
    public List<Map<String, Object>> sessions(@PathVariable final UUID id) {
        return this.sessions.list(this.account(id).id());
    }

    /**
     * Закрыть все сессии учётки.
     * @param id Идентификатор учётки
     */
    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable final UUID id) {
        this.sessions.revoke(this.account(id).id());
    }

    /**
     * Сменить пароль и закрыть все сессии.
     * @param id   Идентификатор учётки
     * @param form Новый пароль
     */
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@PathVariable final UUID id, @RequestBody final Secret form) {
        this.account(id).password(form.password());
        this.sessions.revoke(id);
    }

    /**
     * Назначить роль и закрыть все сессии.
     * @param id   Идентификатор учётки
     * @param form Новая роль
     * @return Учётка
     */
    @PutMapping("/role")
    public Map<String, Object> grant(@PathVariable final UUID id, @RequestBody final Grant form) {
        final Account account = this.account(id);
        account.grant(form.role());
        this.sessions.revoke(id);
        return account.json();
    }

    /**
     * Удалить учётку с её сессиями; повторное удаление ничего не меняет.
     * @param id Идентификатор учётки
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID id) {
        this.accounts.account(id).ifPresent(Account::delete);
    }

    private Account account(final UUID id) {
        return this.accounts.account(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, String.format("Учётка %s не найдена", id)
            )
        );
    }

    /**
     * Новый пароль.
     * @param password Пароль
     * @since 1.0
     */
    public record Secret(String password) {
        /**
         * Конструктор, отвергающий пропущенный пароль.
         * @param password Пароль
         */
        public Secret {
            Objects.requireNonNull(password, "Для смены пароля нужен новый пароль");
        }
    }

    /**
     * Новая роль.
     * @param role Роль
     * @since 1.0
     */
    public record Grant(Role role) {
        /**
         * Конструктор, отвергающий пропущенную роль.
         * @param role Роль
         */
        public Grant {
            Objects.requireNonNull(role, "Для выдачи роли нужна роль");
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import ru.sprbut.m29.identity.Identity;

/**
 * Регистрация: учётка в auth и профиль в profile.
 *
 * <p>Если профиль не создался, учётка удаляется, чтобы не осталось
 * пользователя без профиля.</p>
 *
 * @since 1.0
 */
@RestController
public final class RegistrationEndpoint {

    /**
     * Учётки.
     */
    private final Accounts accounts;

    /**
     * Профили.
     */
    private final Profiles profiles;

    /**
     * Конструктор.
     * @param accounts Учётки
     * @param profiles Профили
     */
    public RegistrationEndpoint(final Accounts accounts, final Profiles profiles) {
        this.accounts = accounts;
        this.profiles = profiles;
    }

    /**
     * Зарегистрировать пользователя с ролью {@link Role#USER}.
     * @param form Данные регистрации
     * @return Учётка
     */
    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody final Registration form) {
        final Account account = this.accounts.add(form.login(), form.password());
        try {
            this.profiles.create(
                new Identity(account.id().toString(), Role.USER.name(), form.login()),
                form.name()
            );
        } catch (final RestClientException ex) {
            account.delete();
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                String.format(
                    "Профиль '%s' не создан, регистрация отменена", form.login()
                ),
                ex
            );
        }
        return ResponseEntity.created(URI.create(String.format("/auth/users/%s", account.id())))
            .body(account.json());
    }

    /**
     * Данные регистрации.
     * @param login    Уникальный логин
     * @param password Пароль
     * @param name     Отображаемое имя
     * @since 1.0
     */
    public record Registration(String login, String password, String name) {
        /**
         * Конструктор, отвергающий пропущенные поля.
         * @param login    Уникальный логин
         * @param password Пароль
         * @param name     Отображаемое имя
         */
        public Registration {
            Objects.requireNonNull(login, "Для регистрации нужен логин");
            Objects.requireNonNull(password, "Для регистрации нужен пароль");
            Objects.requireNonNull(name, "Для регистрации нужно имя");
        }
    }
}

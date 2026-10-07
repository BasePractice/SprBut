/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import ru.sprbut.m29.identity.Caller;

/**
 * HTTP-доступ к профилям: создание, чтение, правка и удаление.
 *
 * <p>Удаление идёт по порядку: характеристики, учётка в auth, профиль.
 * Профиль удаляется последним, чтобы при сбое удаление можно было повторить.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/profiles")
public final class ProfilesEndpoint {

    /**
     * Профили.
     */
    private final Profiles profiles;

    /**
     * Характеристики.
     */
    private final Parameters parameters;

    /**
     * Учётки в auth.
     */
    private final Accounts accounts;

    /**
     * Конструктор.
     * @param profiles   Профили
     * @param parameters Характеристики
     * @param accounts   Учётки в auth
     */
    public ProfilesEndpoint(
        final Profiles profiles,
        final Parameters parameters,
        final Accounts accounts
    ) {
        this.profiles = profiles;
        this.parameters = parameters;
        this.accounts = accounts;
    }

    /**
     * Завести профиль; это делает auth от имени нового пользователя.
     * @param form   Данные профиля
     * @param caller Пользователь из заголовков
     * @return Профиль
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
        @RequestBody final Draft form,
        final Authentication caller
    ) {
        if (!new Caller(caller).owns(form.id().toString())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                String.format(
                    "Пользователь %s не может завести чужой профиль %s",
                    caller.getName(), form.id()
                )
            );
        }
        final Profile profile = this.profiles.add(form.id(), form.login(), form.name());
        return ResponseEntity.created(URI.create(String.format("/profiles/%s", profile.id())))
            .body(profile.json());
    }

    /**
     * Все профили.
     * @return Профили
     */
    @GetMapping
    public List<Map<String, Object>> list() {
        return this.profiles.all().stream().map(Profile::json).toList();
    }

    /**
     * Профиль.
     * @param id Идентификатор
     * @return Профиль
     */
    @GetMapping("/{id}")
    public Map<String, Object> read(@PathVariable final UUID id) {
        return this.profile(id).json();
    }

    /**
     * Сменить имя.
     * @param id   Идентификатор
     * @param form Новое имя
     * @return Профиль
     */
    @PutMapping("/{id}")
    public Map<String, Object> rename(
        @PathVariable final UUID id,
        @RequestBody final Rename form
    ) {
        final Profile profile = this.profile(id);
        profile.rename(form.name());
        return profile.json();
    }

    /**
     * Удалить пользователя целиком: характеристики, учётку и профиль.
     * @param id     Идентификатор
     * @param caller Пользователь из заголовков
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID id, final Authentication caller) {
        final Profile profile = this.profile(id);
        this.parameters.delete(id);
        this.accounts.delete(new Caller(caller).identity(), id);
        profile.delete();
    }

    private Profile profile(final UUID id) {
        return this.profiles.profile(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, String.format("Профиль %s не найден", id)
            )
        );
    }

    /**
     * Данные нового профиля.
     * @param id    Идентификатор учётки в auth
     * @param login Логин
     * @param name  Отображаемое имя
     * @since 1.0
     */
    public record Draft(UUID id, String login, String name) {
        /**
         * Конструктор, отвергающий пропущенные поля.
         * @param id    Идентификатор учётки в auth
         * @param login Логин
         * @param name  Отображаемое имя
         */
        public Draft {
            Objects.requireNonNull(id, "Для профиля нужен идентификатор");
            Objects.requireNonNull(login, "Для профиля нужен логин");
            Objects.requireNonNull(name, "Для профиля нужно имя");
        }
    }

    /**
     * Новое имя.
     * @param name Отображаемое имя
     * @since 1.0
     */
    public record Rename(String name) {
        /**
         * Конструктор, отвергающий пропущенное имя.
         * @param name Отображаемое имя
         */
        public Rename {
            Objects.requireNonNull(name, "Для переименования нужно новое имя");
        }
    }
}

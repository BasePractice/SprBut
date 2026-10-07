/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * HTTP-доступ к характеристикам пользователя, хранящимся в сервисе параметров.
 * @since 1.0
 */
@RestController
@RequestMapping("/profiles/{id}/parameters")
public final class CharacteristicsEndpoint {

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
    public CharacteristicsEndpoint(final Profiles profiles, final Parameters parameters) {
        this.profiles = profiles;
        this.parameters = parameters;
    }

    /**
     * Все характеристики пользователя.
     * @param id Идентификатор пользователя
     * @return Значения по именам
     */
    @GetMapping
    public Map<String, String> all(@PathVariable final UUID id) {
        return this.parameters.all(this.owner(id));
    }

    /**
     * Значение характеристики.
     * @param id   Идентификатор пользователя
     * @param name Имя характеристики
     * @return Значение
     */
    @GetMapping(path = "/{name}", produces = "text/plain;charset=UTF-8")
    public String value(@PathVariable final UUID id, @PathVariable final String name) {
        return this.parameters.value(this.owner(id), name).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                String.format("Характеристика '%s' пользователя %s не найдена", name, id)
            )
        );
    }

    /**
     * Сохранить значение характеристики.
     * @param id    Идентификатор пользователя
     * @param name  Имя характеристики
     * @param value Значение
     */
    @PutMapping(path = "/{name}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void put(
        @PathVariable final UUID id,
        @PathVariable final String name,
        @RequestBody final String value
    ) {
        this.parameters.put(this.owner(id), name, value);
    }

    private UUID owner(final UUID id) {
        return this.profiles.profile(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, String.format("Профиль %s не найден", id)
            )
        ).id();
    }
}

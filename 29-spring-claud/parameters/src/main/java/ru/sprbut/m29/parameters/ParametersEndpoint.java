/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.parameters;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * HTTP-доступ к параметрам владельцев.
 * @since 1.0
 */
@RestController
@RequestMapping("/parameters/{owner}")
public final class ParametersEndpoint {

    /**
     * Параметры.
     */
    private final Parameters parameters;

    /**
     * Конструктор.
     * @param parameters Параметры
     */
    public ParametersEndpoint(final Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Все параметры владельца.
     * @param owner Владелец
     * @return Значения по именам
     */
    @GetMapping
    public Map<String, String> all(@PathVariable final String owner) {
        return this.parameters.all(owner);
    }

    /**
     * Значение параметра.
     * @param owner Владелец
     * @param name  Имя параметра
     * @return Значение
     */
    @GetMapping(path = "/{name}", produces = "text/plain;charset=UTF-8")
    public String value(@PathVariable final String owner, @PathVariable final String name) {
        return this.parameters.value(owner, name).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                String.format("Характеристика '%s' владельца '%s' не найдена", name, owner)
            )
        );
    }

    /**
     * Сохранить значение параметра.
     * @param owner Владелец
     * @param name  Имя параметра
     * @param value Значение
     */
    @PutMapping(path = "/{name}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void put(
        @PathVariable final String owner,
        @PathVariable final String name,
        @RequestBody final String value
    ) {
        this.parameters.put(owner, name, value);
    }

    /**
     * Удалить все параметры владельца.
     * @param owner Владелец
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final String owner) {
        this.parameters.delete(owner);
    }
}

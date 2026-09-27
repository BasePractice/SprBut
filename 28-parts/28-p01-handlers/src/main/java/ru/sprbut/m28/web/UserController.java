/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m28.dto.CreateUserRequest;
import ru.sprbut.m28.dto.Login;
import ru.sprbut.m28.dto.UserDto;
import ru.sprbut.m28.handlers.CurrentUser;

/**
 * Контроллер, который ничего не делает сам.
 *
 * <p>Каждый метод здесь — три строки, и это условие задачи: интересное
 * происходит до вызова. Логин уже приведён к нижнему регистру конвертером,
 * почта в теле уже замаскирована советом, имя текущего пользователя уже
 * собрано резолвером из заголовка. Контроллер видит только результат.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
public final class UserController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public UserController() {
        // нечего инициализировать
    }

    /**
     * Приветствие: значение параметра приходит уже конвертированным.
     * @param name Логин, собранный конвертером из строки запроса
     * @return Приветствие
     */
    @GetMapping("/greet")
    public String greet(@RequestParam final Login name) {
        return String.format("Здравствуйте, %s", name.value());
    }

    /**
     * Создание пользователя: тело приходит уже разобранным и обработанным.
     * @param request Тело запроса
     * @return Созданный пользователь
     */
    @PostMapping
    public UserDto create(@Valid @RequestBody final CreateUserRequest request) {
        return new UserDto(request.username(), request.email());
    }

    /**
     * Текущий пользователь: аргумент собран не из запроса, а резолвером.
     * @param user Имя текущего пользователя
     * @return Имя текущего пользователя
     */
    @GetMapping("/me")
    public String current(@CurrentUser final String user) {
        return user;
    }
}

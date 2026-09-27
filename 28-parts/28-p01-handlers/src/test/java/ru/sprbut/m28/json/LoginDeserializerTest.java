/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.json;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m28.dto.Login;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Разбор логина из JSON.
 * @since 1.0
 */
final class LoginDeserializerTest {

    @Test
    @DisplayName("логин из JSON приходит в нижнем регистре и без пробелов")
    void lowersLogin() {
        MatcherAssert.assertThat(
            "логин из JSON не приведён к нижнему регистру",
            JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Login.class, new LoginDeserializer()))
                .build()
                .readValue("\" FeDor \"", Login.class),
            Matchers.is(new Login("fedor"))
        );
    }

    @Test
    @DisplayName("объект вместо строки отвергается как ошибка разбора")
    void rejectsObject() {
        Assertions.assertThrows(
            JacksonException.class,
            () -> JsonMapper.builder()
                .addModule(new SimpleModule().addDeserializer(Login.class, new LoginDeserializer()))
                .build()
                .readValue("{\"first\":\"fedor\"}", Login.class),
            "объект принят за логин"
        );
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.advices;

import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import ru.sprbut.m28.dto.CreateUserRequest;

/**
 * Совет над телом запроса.
 * @since 1.0
 */
final class MaskingRequestBodyAdviceTest {

    @Test
    @DisplayName("совет берётся за тело своего типа")
    void supportsOwnBody() {
        MatcherAssert.assertThat(
            "совет не узнал своё тело",
            new MaskingRequestBodyAdvice().supports(
                null, CreateUserRequest.class, JacksonJsonHttpMessageConverter.class
            ),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("совет не трогает чужое тело")
    void skipsForeignBody() {
        MatcherAssert.assertThat(
            "совет взялся за чужое тело",
            new MaskingRequestBodyAdvice().supports(
                null, String.class, JacksonJsonHttpMessageConverter.class
            ),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("от почты остаётся первая буква и домен")
    void masksEmail() {
        MatcherAssert.assertThat(
            "почта дошла до контроллера незамаскированной",
            new MaskingRequestBodyAdvice().afterBodyRead(
                new CreateUserRequest("ivan", "ivan@example.com"),
                new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8)),
                null, CreateUserRequest.class, JacksonJsonHttpMessageConverter.class
            ),
            Matchers.is(new CreateUserRequest("ivan", "i***@example.com"))
        );
    }

    @Test
    @DisplayName("тело без почты доходит до проверки как есть")
    void keepsBodyWithoutEmail() {
        MatcherAssert.assertThat(
            "тело без почты изменено советом",
            new MaskingRequestBodyAdvice().afterBodyRead(
                new CreateUserRequest("ivan", null),
                new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8)),
                null, CreateUserRequest.class, JacksonJsonHttpMessageConverter.class
            ),
            Matchers.is(new CreateUserRequest("ivan", null))
        );
    }
}

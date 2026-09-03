/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Тело запроса на создание заметки.
 *
 * <p>Аннотации проверки — это метаданные, и сами по себе они ничего
 * не запрещают: работать их заставляет {@code @Valid} в контроллере,
 * а ошибку в ответ превращает {@link Failures}.</p>
 *
 * @param text Текст заметки
 * @since 1.0
 */
public record NewNoteRequest(

    @NotBlank(message = "текст заметки обязателен")
    @Size(max = 200, message = "текст длиннее 200 символов")
    String text
) {
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Тело запроса на создание задачи.
 *
 * <p>{@code record} закрывает ровно ту потребность, ради которой в JavaBeans
 * заводили геттеры и сеттеры: неизменяемые данные с готовыми
 * {@code equals}, {@code hashCode} и {@code toString}.</p>
 *
 * <p>Аннотации проверки — снова метаданные: сами по себе они ничего не запрещают,
 * работать их заставляет {@code @Valid} в контроллере.</p>
 *
 * @since 1.0
 */
public record NewTaskRequest(

    @NotBlank(message = "название задачи обязательно")
    @Size(max = 200, message = "название длиннее 200 символов")
    String title
) {
}

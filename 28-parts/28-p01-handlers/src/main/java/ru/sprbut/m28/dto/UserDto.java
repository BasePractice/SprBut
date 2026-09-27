/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

/**
 * Ответ о созданном пользователе.
 *
 * <p>Отдельный от {@link CreateUserRequest} тип: в ответ уходит не то же
 * самое, что пришло в запросе, и правила проверки к ответу отношения
 * не имеют.</p>
 *
 * @param username Имя пользователя
 * @param email Почта пользователя, уже замаскированная советом
 * @since 1.0
 */
public record UserDto(String username, String email) {
}

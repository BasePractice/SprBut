/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

/**
 * Record, компоненты которого помечены отдельно от полей.
 * @param id    идентификатор
 * @param login имя пользователя
 * @since 1.0
 */
public record UserRow(@Column(name = "user_id") String id, @Column(name = "login") String login) {
}

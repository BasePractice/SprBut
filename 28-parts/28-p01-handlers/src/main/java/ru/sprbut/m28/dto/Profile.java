/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

/**
 * Анкета в ответе: форма плюс то, что добавил сервер.
 *
 * @param name Имя
 * @param city Город
 * @param region Регион, собранный методом {@code @ModelAttribute}
 * @since 1.0
 */
public record Profile(String name, String city, String region) {
}

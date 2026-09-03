/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.domain;

/**
 * Заметка: минимальный предмет, чтобы разговор шёл про веб, а не про домен.
 *
 * <p>{@code record} здесь не только короче класса: Jackson знает про компоненты
 * записи и сериализует их без геттеров JavaBeans.</p>
 *
 * @param id Номер заметки
 * @param text Текст заметки
 * @since 1.0
 */
public record Note(long id, String text) {
}

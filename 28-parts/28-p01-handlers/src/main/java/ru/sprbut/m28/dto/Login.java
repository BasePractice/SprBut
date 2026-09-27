/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

/**
 * Логин: строка запроса, доведённая до доменного типа.
 *
 * <p>Ради него и существует конвертер. Пока параметр объявлен как
 * {@code String}, конвертировать нечего: {@code ConversionService}
 * не вызывает преобразование из типа в него же, и
 * {@code Converter<String, String>} остаётся мёртвым кодом. Как только
 * у параметра появляется собственный тип, конвертер становится
 * единственным способом его получить.</p>
 *
 * @param value Значение логина в нижнем регистре
 * @since 1.0
 */
public record Login(String value) {
}

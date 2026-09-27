/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.filters;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Запрос с подменённым телом.
 *
 * <p>{@code HttpServletRequestWrapper} — штатный способ изменить запрос
 * по дороге: обёртка делегирует все методы исходному запросу и
 * переопределяет только те два, через которые читают тело.</p>
 *
 * @since 1.0
 */
public final class SanitizedRequest extends HttpServletRequestWrapper {

    /**
     * Вычищенное тело запроса.
     */
    private final byte[] body;

    /**
     * Основной конструктор.
     * @param request Исходный запрос
     * @param body Вычищенное тело запроса
     */
    public SanitizedRequest(final HttpServletRequest request, final byte[] body) {
        super(request);
        this.body = body.clone();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new SanitizedStream(this.body);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(
            new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8)
        );
    }
}

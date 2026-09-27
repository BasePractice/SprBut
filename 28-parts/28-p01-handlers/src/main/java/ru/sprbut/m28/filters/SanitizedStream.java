/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.filters;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;

/**
 * Поток над массивом байтов.
 *
 * <p>{@code ServletInputStream} — абстрактный класс, а не интерфейс, и
 * трёх методов асинхронного чтения он требует даже от потока, который
 * целиком лежит в памяти и всегда готов.</p>
 *
 * @since 1.0
 */
public final class SanitizedStream extends ServletInputStream {

    /**
     * Тело запроса.
     */
    private final ByteArrayInputStream body;

    /**
     * Основной конструктор.
     * @param bytes Тело запроса
     */
    public SanitizedStream(final byte[] bytes) {
        super();
        this.body = new ByteArrayInputStream(bytes.clone());
    }

    @Override
    public int read() {
        return this.body.read();
    }

    @Override
    public int read(final byte[] bytes, final int off, final int len) {
        return this.body.read(bytes, off, len);
    }

    @Override
    public boolean isFinished() {
        return this.body.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(final ReadListener listener) {
        // тело уже в памяти: асинхронное чтение здесь не нужно
    }
}

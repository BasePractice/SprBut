/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.members;

/**
 * Два использования одной аннотации: минимальное и полное.
 *
 * <p>Пара нужна целиком: только сравнив их, видно, что незаданный элемент
 * и элемент, заданный своим же умолчанием, в рефлексии неразличимы.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public class Service {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Service() {
        // нечего инициализировать
    }

    /**
     * Задан только обязательный элемент, остальные берутся из умолчаний.
     */
    @Operation(name = "минимум")
    public void withDefaults() {
    }

    /**
     * Заданы все элементы всех допустимых типов.
     */
    @Operation(
        name = "полный",
        timeout = 5,
        readOnly = true,
        rollbackFor = IllegalStateException.class,
        isolation = Isolation.SERIALIZABLE,
        retry = @Retry(attempts = 3, backoffMillis = 250),
        tags = {"critical", "billing"},
        handles = {String.class, Integer.class},
        allowed = {Isolation.READ_COMMITTED, Isolation.SERIALIZABLE}
    )
    public void withEverything() {
    }
}

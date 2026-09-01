/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Marker;

/**
 * Родитель с аннотацией без {@code @Inherited} — на подкласс она не перейдёт.
 * @since 1.0
 */
@Marker
public class MarkedParent {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MarkedParent() {
        // нечего инициализировать
    }
}

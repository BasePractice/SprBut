/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m10.lombok.samples;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * {@code @Builder} с {@code @Singular}: коллекция наполняется по одному
 * элементу и становится неизменяемой в собранном объекте.
 *
 * <p>Именно это отличает билдер от JavaBeans-подхода: объект нельзя увидеть
 * недособранным, потому что до вызова {@code build()} его не существует.</p>
 *
 * @since 1.0
 */
@Builder
@Getter
public class Order {

    /**
     * Номер.
     */
    private final String number;

    /**
     * Значение {@code items}.
     */
    @Singular
    private final List<String> items;
}

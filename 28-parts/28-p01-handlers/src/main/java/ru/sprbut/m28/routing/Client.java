/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.routing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метка метода, который обслуживает только определённый вид клиента.
 *
 * <p>Путь, метод HTTP, заголовки и тип содержимого — условия выбора
 * обработчика, которые {@code @RequestMapping} знает сам. Эта аннотация
 * добавляет своё условие, и читает её {@link ClientHandlerMapping}:
 * без него метка останется просто меткой.</p>
 *
 * @since 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Client {

    /**
     * Вид клиента из заголовка {@code X-Client}.
     * @return Вид клиента
     */
    String value();
}

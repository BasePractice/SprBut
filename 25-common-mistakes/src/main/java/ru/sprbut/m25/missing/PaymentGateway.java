/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.missing;

/**
 * Зависимость, которую забыли зарегистрировать в контексте.
 *
 * <p>Интерфейс существует, класс-реализация существует, но ни одна конфигурация
 * не объявляет бин — контейнер узнаёт об этом только в момент внедрения.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface PaymentGateway {

    /**
     * Идентификатор платёжного канала.
     * @return Название канала
     */
    String channel();
}

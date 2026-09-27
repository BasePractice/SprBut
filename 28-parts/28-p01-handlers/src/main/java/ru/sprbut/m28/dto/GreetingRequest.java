/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Тело запроса на приветствие.
 *
 * <p>Тот же {@link Login}, что и в строке запроса, но путь у него другой:
 * тело разбирает Jackson, и {@code LowerCaseConverter} в этом не
 * участвует. Строку в логин здесь превращает и приводит к нижнему
 * регистру {@code LoginDeserializer}.</p>
 *
 * <p>Поле без значения Jackson оставляет пустым, не вызывая разборщик.
 * Правило {@code @NotNull} вместе с {@code @Valid} в контроллере
 * отвечает на это кодом 400; без них метод упал бы на
 * {@code name().value()} и клиент получил бы 500.</p>
 *
 * @param name Логин
 * @since 1.0
 */
public record GreetingRequest(@NotNull(message = "логин обязателен") Login name) {
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m23.web;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * Представление задачи для API.
 *
 * <p>Здесь курс замыкается сам на себя: {@code @Value} и {@code @Builder} —
 * это Lombok, то есть annotation processor, который дописывает класс
 * во время компиляции. Поля становятся {@code private final}, класс —
 * {@code final}, конструктор и билдер генерируются.</p>
 *
 * <p>Соседний {@link NewTaskRequest} делает почти то же самое штатным
 * {@code record}: наглядное сравнение генерации кода и языковой конструкции.</p>
 *
 * @since 1.0
 */
@Value
@Builder
public class TaskView {

    /**
     * Идентификатор.
     */
    Long id;

    /**
     * Название.
     */
    String title;

    /**
     * Статус.
     */
    String status;

    /**
     * Момент создания.
     */
    Instant created;
}

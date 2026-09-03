/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import ru.sprbut.m27.web.NewTaskRequest;
import ru.sprbut.m27.web.TaskView;

/**
 * Подсказки для native image.
 *
 * <p>Классы DTO нигде не создаются явно: их собирает Jackson рефлексией по данным
 * запроса. Для графа достижимости GraalVM это невидимая связь, и без объявления
 * образ соберётся, запустится и упадёт на первом же обращении к API.</p>
 *
 * @since 1.0
 */
public final class TrackerHints implements RuntimeHintsRegistrar {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TrackerHints() {
        // нечего инициализировать
    }

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classes) {
        hints.reflection()
            .registerType(NewTaskRequest.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(TaskView.class, MemberCategory.INVOKE_DECLARED_METHODS);
    }
}

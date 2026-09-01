/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Общий контракт для трёх реализаций одного и того же маппинга.
 *
 * <p>Слайд 78 (СХЕМА 4) противопоставляет compile-time и runtime. Здесь это
 * противопоставление становится проверяемым: три реализации, один интерфейс,
 * один и тот же набор тестов.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface UserMapper {

    /**
     * Объект передачи данных.
     * @param entity Сущность
     * @return Объект передачи данных
     */
    UserDto toDto(UserEntity entity);

    /**
     * Как именно реализация узнаёт, что куда копировать.
     * @return Как именно реализация узнаёт, что куда копировать
     */
    String strategy();
}

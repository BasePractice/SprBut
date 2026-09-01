/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

import net.bytebuddy.implementation.bind.annotation.Argument;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Тело метода {@code toDto} у сгенерированного класса.
 *
 * <p>Метод обязан быть {@code static} — так устроен {@code MethodDelegation}
 * в ByteBuddy. Тот же код, что MapStruct пишет на этапе компиляции,
 * здесь появляется в runtime.</p>
 *
 * @since 1.0
 */
public final class CopyInterceptor {

    private CopyInterceptor() {
    }

    /**
     * Копирует поля сущности в DTO.
     * @param entity Сущность
     * @return Копирует поля сущности в DTO
     */
    public static UserDto map(final @Argument(0) UserEntity entity) {
        if (entity == null) {
            return null;
        }
        final UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAge(entity.getAge());
        dto.setActive(entity.isActive());
        return dto;
    }
}

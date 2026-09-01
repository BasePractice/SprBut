/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m09;

import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Слайд 74: «APT: compile-time, только генерация, быстро».
 *
 * <p>Этот класс написан так, как его написал бы annotation processor:
 * прямые вызовы геттеров и сеттеров, без единого обращения к метаданным.
 * Ровно такой код генерирует MapStruct (модуль 10).</p>
 *
 * <p>Свойства подхода — зеркальное отражение рефлексии:
 * <ul>
 * <li><b>быстро</b>: обычные вызовы методов, JIT инлайнит их без ограничений;</li>
 * <li><b>безопасно</b>: несовпадение типов — ошибка компиляции, а не runtime;</li>
 * <li><b>негибко</b>: добавили поле — нужна пересборка. Ничего нельзя решить
 * по данным, известным только во время работы;</li>
 * <li><b>работает в native image</b> — рефлексии нет вовсе (модуль 22).</li>
 * </ul></p>
 *
 * @since 1.0
 */
public final class GeneratedStyleMapper implements UserMapper {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public GeneratedStyleMapper() {
        // нечего инициализировать
    }

    // свойства перечислены вручную: internalNote в DTO отсутствует,
    // и процессор увидел бы это ещё при сборке
    @Override
    public UserDto toDto(final UserEntity entity) {
        final UserDto dto;
        if (entity == null) {
            dto = null;
        } else {
            dto = new UserDto();
            dto.setId(entity.getId());
            dto.setFirstName(entity.getFirstName());
            dto.setLastName(entity.getLastName());
            dto.setAge(entity.getAge());
            dto.setActive(entity.isActive());
        }
        return dto;
    }

    @Override
    public String strategy() {
        return "APT: правила зафиксированы в коде на этапе компиляции";
    }
}

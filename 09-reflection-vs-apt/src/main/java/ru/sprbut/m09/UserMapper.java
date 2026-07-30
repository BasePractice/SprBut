package ru.sprbut.m09;

import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Общий контракт для трёх реализаций одного и того же маппинга.
 * <p>
 * Слайд 78 (СХЕМА 4) противопоставляет compile-time и runtime. Здесь это
 * противопоставление становится проверяемым: три реализации, один интерфейс,
 * один и тот же набор тестов.
 */
public interface UserMapper {

    UserDto toDto(UserEntity entity);

    /** Как именно реализация узнаёт, что куда копировать. */
    String strategy();
}

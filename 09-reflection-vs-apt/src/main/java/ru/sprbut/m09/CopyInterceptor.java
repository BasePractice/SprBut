package ru.sprbut.m09;

import net.bytebuddy.implementation.bind.annotation.Argument;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Тело метода {@code toDto} у сгенерированного класса.
 * <p>
 * Метод обязан быть {@code static} — так устроен {@code MethodDelegation}
 * в ByteBuddy. Тот же код, что MapStruct пишет на этапе компиляции,
 * здесь появляется в runtime.
 */
public final class CopyInterceptor {

    private CopyInterceptor() {
    }

    /**
     * Копирует поля сущности в DTO.
     */
    public static UserDto map(@Argument(0) UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAge(entity.getAge());
        dto.setActive(entity.isActive());
        return dto;
    }
}

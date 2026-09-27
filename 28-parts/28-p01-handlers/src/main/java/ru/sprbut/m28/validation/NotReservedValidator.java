/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

/**
 * Исполнитель правила {@link NotReserved}.
 *
 * <p>Пустое значение правило пропускает: за обязательность отвечает
 * {@code @NotBlank}, и каждое правило проверяет одно. Иначе на пустое имя
 * клиент получил бы два пункта в перечне ошибок вместо одного.</p>
 *
 * <p>Экземпляр создаёт не Hibernate Validator, а Spring: исполнитель может
 * получить зависимости через конструктор, как обычный бин.</p>
 *
 * @since 1.0
 */
public final class NotReservedValidator implements ConstraintValidator<NotReserved, String> {

    /**
     * Открытый конструктор: экземпляр создаёт фабрика исполнителей.
     */
    public NotReservedValidator() {
        // нечего инициализировать
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return value == null
            || !Set.of("admin", "root", "system").contains(value.trim().toLowerCase(Locale.ROOT));
    }
}

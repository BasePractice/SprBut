/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Правило проверки: имя не из числа зарезервированных.
 *
 * <p>Готовые правила — {@code @NotBlank}, {@code @Size},
 * {@code @Pattern} — проверяют форму значения. Правило предметной
 * области приходится писать самому, и выглядит оно так же: аннотация
 * с тремя обязательными атрибутами и класс, который её исполняет.</p>
 *
 * @since 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotReservedValidator.class)
public @interface NotReserved {

    /**
     * Сообщение о нарушении.
     * @return Сообщение
     */
    String message() default "имя зарезервировано системой";

    /**
     * Группы проверки.
     * @return Группы
     */
    Class<?>[] groups() default {};

    /**
     * Дополнительные сведения о нарушении.
     * @return Сведения
     */
    Class<? extends Payload>[] payload() default {};
}

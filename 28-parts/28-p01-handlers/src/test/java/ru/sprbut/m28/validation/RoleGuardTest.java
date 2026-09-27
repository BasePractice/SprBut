/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.validation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import ru.sprbut.m28.dto.ProfileForm;

/**
 * Проверка самоназначенной роли.
 * @since 1.0
 */
final class RoleGuardTest {

    @Test
    @DisplayName("роль, присланная клиентом, отмечается ошибкой на своём поле")
    void rejectsRole() {
        final ProfileForm form = new ProfileForm("Нина", "Томск", "ADMIN");
        final Errors errors = new BeanPropertyBindingResult(form, "profile");
        new RoleGuard().validate(form, errors);
        MatcherAssert.assertThat(
            "самоназначенная роль прошла проверку",
            errors.hasFieldErrors("role"),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("анкета без роли проходит проверку")
    void acceptsFormWithoutRole() {
        final ProfileForm form = new ProfileForm("Нина", "Томск", null);
        final Errors errors = new BeanPropertyBindingResult(form, "profile");
        new RoleGuard().validate(form, errors);
        MatcherAssert.assertThat(
            "анкета без роли отвергнута",
            errors.hasErrors(),
            Matchers.is(false)
        );
    }
}

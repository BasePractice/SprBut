/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.validation;

import org.jspecify.annotations.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.sprbut.m28.dto.ProfileForm;

/**
 * Проверка Spring: роль в анкете назначает не клиент.
 *
 * <p>Не всякое правило ложится в аннотацию. Это зависит от того, какой
 * метод принимает форму: администратор роль назначать может, клиент в
 * своей анкете — нет. Поэтому правило не висит на поле, а подключается
 * к связывателю одного контроллера через {@code @InitBinder}.</p>
 *
 * <p>{@code Validator} Spring старше Bean Validation и проще: объект
 * целиком на входе, ошибки по полям на выходе. Вызывает его тот же
 * {@code @Valid}, что и аннотации, и нарушения попадают в тот же
 * перечень ошибок.</p>
 *
 * @since 1.0
 */
public final class RoleGuard implements Validator {

    /**
     * Открытый конструктор: экземпляр создаёт контроллер.
     */
    public RoleGuard() {
        // нечего инициализировать
    }

    @Override
    public boolean supports(final @NonNull Class<?> type) {
        return ProfileForm.class.equals(type);
    }

    @Override
    public void validate(final @NonNull Object target, final @NonNull Errors errors) {
        if (((ProfileForm) target).role() != null) {
            errors.rejectValue("role", "self.assigned", "роль назначает сервер");
        }
    }
}

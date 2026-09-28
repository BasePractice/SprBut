/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.advices;

import java.lang.reflect.Type;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import ru.sprbut.m28.dto.CreateUserRequest;

/**
 * Пятый участник: совет над телом запроса.
 *
 * <p>Совет вклинивается между конвертером сообщений и контроллером: JSON
 * уже разобран в объект, метод ещё не вызван. Место редкое, и занимают его
 * ради сквозных правил — маскирования, подстановки умолчаний, аудита.</p>
 *
 * <p>{@code supports} обязан ограничить совет своим типом. Возвращая
 * {@code true} на любое тело, совет получал бы все тела всех контроллеров,
 * а приведение внутри {@code afterBodyRead} падало бы на первом чужом
 * запросе.</p>
 *
 * <p>Порядок важен: совет срабатывает до проверки {@code @Valid}, поэтому
 * почта здесь ещё не обязана быть почтой.</p>
 *
 * @since 1.0
 */
@ControllerAdvice
public final class MaskingRequestBodyAdvice extends RequestBodyAdviceAdapter {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MaskingRequestBodyAdvice() {
        super();
    }

    @Override
    public boolean supports(
            final @NonNull MethodParameter parameter, final @NonNull Type target,
            final @NonNull Class<? extends HttpMessageConverter<?>> converter
    ) {
        return CreateUserRequest.class.equals(target);
    }

    @Override
    public Object afterBodyRead(
            final @NonNull Object body, final @NonNull HttpInputMessage input, final @NonNull MethodParameter parameter,
            final @NonNull Type target, final @NonNull Class<? extends HttpMessageConverter<?>> converter
    ) {
        final CreateUserRequest request = (CreateUserRequest) body;
        final Object read;
        if (request.email() == null) {
            read = body;
        } else {
            read = new CreateUserRequest(
                request.username(), MaskingRequestBodyAdvice.mask(request.email())
            );
        }
        return read;
    }

    // от адреса остаётся первая буква и домен: в лог такое писать уже можно
    private static String mask(final String email) {
        final int pos = email.indexOf('@');
        final String masked;
        if (pos < 1) {
            masked = email;
        } else {
            masked = String.format("%c***%s", email.charAt(0), email.substring(pos));
        }
        return masked;
    }
}

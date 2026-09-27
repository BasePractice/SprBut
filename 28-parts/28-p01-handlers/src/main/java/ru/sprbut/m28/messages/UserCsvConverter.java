/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ProtectedMethodInFinalClassCheck disable
package ru.sprbut.m28.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import ru.sprbut.m28.dto.UserDto;

/**
 * Конвертер сообщений: пользователь в CSV.
 *
 * <p>Конвертер сообщений превращает объект в байты тела и обратно.
 * Какой из них возьмётся за ответ, решает согласование: диспетчер
 * собирает типы, которые умеют записать все подходящие конвертеры,
 * сверяет их с заголовком {@code Accept} и берёт первый
 * совместимый.</p>
 *
 * <p>«Первый» — ключевое слово. Клиент без {@code Accept} или
 * с согласием на любой тип примет что угодно, и победит
 * конвертер, стоящий в списке раньше. Поставленный впереди штатных,
 * этот конвертер перехватил бы все ответы с {@code UserDto}, и JSON
 * получали бы только те, кто попросил его явно. Поэтому
 * {@code HandlersConfig} добавляет его в конец списка.</p>
 *
 * <p>Конвертер только пишет: читать CSV никто не просил. Строки
 * разделены парой CR LF, как требует RFC 4180, на любой ОС.</p>
 *
 * @since 1.0
 */
public final class UserCsvConverter extends AbstractHttpMessageConverter<UserDto> {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public UserCsvConverter() {
        super(StandardCharsets.UTF_8, new MediaType("text", "csv"));
    }

    @Override
    public boolean canRead(final Class<?> type, final MediaType media) {
        return false;
    }

    @Override
    protected boolean supports(final Class<?> type) {
        return UserDto.class.equals(type);
    }

    @Override
    protected UserDto readInternal(
        final Class<? extends UserDto> type, final HttpInputMessage input
    ) {
        throw new HttpMessageNotReadableException("пользователь в CSV не читается", input);
    }

    @Override
    protected void writeInternal(final UserDto user, final HttpOutputMessage output)
        throws IOException {
        UserCsvConverter.line(output.getBody(), "username,email");
        UserCsvConverter.line(
            output.getBody(), String.format("%s,%s", user.username(), user.email())
        );
    }

    private static void line(final OutputStream body, final String text) throws IOException {
        body.write(text.getBytes(StandardCharsets.UTF_8));
        body.write('\r');
        body.write('\n');
    }
}

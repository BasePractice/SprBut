/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.json;

import java.util.Locale;
import java.util.Objects;
import org.springframework.boot.jackson.JacksonComponent;
import ru.sprbut.m28.dto.Login;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Разбор логина из JSON.
 *
 * <p>У значения два пути внутрь метода. Строку запроса, путь и заголовки
 * преобразует {@code ConversionService} — там работает
 * {@code LowerCaseConverter}. Тело разбирает конвертер сообщений, то есть
 * Jackson, и о конвертерах Spring он ничего не знает. Без этого класса
 * запрос не дошёл бы до метода вовсе: запись {@code Login} Jackson
 * собирает по свойствам и ждёт {@code {"value": ...}}, а получает строку,
 * и клиент видит 400.</p>
 *
 * <p>Правило «логин в нижнем регистре» поэтому приходится повторить на
 * стороне Jackson. {@code @JacksonComponent} регистрирует разборщик в том
 * {@code JsonMapper}, который Boot отдаёт конвертеру сообщений: свой
 * модуль собирать не нужно.</p>
 *
 * <p>{@code getValueAsString} возвращает {@code null}, если вместо строки
 * пришёл объект или массив. Голый {@code NullPointerException} Jackson
 * пропустил бы наружу как есть, поэтому отказ оформлен его же средством,
 * {@code reportInputMismatch}: это ошибка разбора, и клиент получает 400.</p>
 *
 * @since 1.0
 */
@JacksonComponent
public final class LoginDeserializer extends ValueDeserializer<Login> {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LoginDeserializer() {
        super();
    }

    @Override
    public Login deserialize(final JsonParser parser, final DeserializationContext context) {
        return new Login(
            Objects.requireNonNullElseGet(
                parser.getValueAsString(),
                () -> context.<String>reportInputMismatch(this, "логин в JSON должен быть строкой")
            ).trim()
                .toLowerCase(Locale.ROOT)
        );
    }
}

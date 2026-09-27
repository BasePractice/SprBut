/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.converters;

import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.sprbut.m28.dto.Login;

/**
 * Третий участник: конвертер значения.
 *
 * <p>Строка запроса приходит строкой всегда. Тип параметра метода —
 * требование к значению, и выполняет его {@code ConversionService}:
 * он подбирает конвертер по паре «откуда, куда» и вызывает его до того,
 * как контроллер получит управление.</p>
 *
 * <p>Пара типов и есть условие работы. {@code Converter<String, String>}
 * не вызовется никогда: преобразовывать строку в строку
 * {@code ConversionService} не станет, и приведение к нижнему регистру,
 * написанное в таком конвертере, молча не случится.</p>
 *
 * <p>Конвертер-бин Boot подхватывает сам: автоконфигурация MVC собирает
 * все бины {@code Converter} в {@code mvcConversionService}.</p>
 *
 * @since 1.0
 */
@Component
public final class LowerCaseConverter implements Converter<String, Login> {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LowerCaseConverter() {
        // нечего инициализировать
    }

    @Override
    public Login convert(final String source) {
        return new Login(source.trim().toLowerCase(Locale.ROOT));
    }
}

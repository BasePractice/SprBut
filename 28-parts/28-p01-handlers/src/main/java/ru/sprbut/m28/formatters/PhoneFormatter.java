/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.formatters;

import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.format.Formatter;
import ru.sprbut.m28.dto.Phone;

/**
 * Форматтер телефона.
 *
 * <p>{@code Formatter} — конвертер, который умеет обе стороны и знает
 * локаль: {@code parse} превращает текст в объект, {@code print} —
 * обратно. Обратная сторона нужна шаблонам и полям форм; ответ
 * {@code @ResponseBody} пишет Jackson, и {@code print} для него
 * не вызывается.</p>
 *
 * <p>Номер, в котором не десять и не одиннадцать цифр, отвергается
 * исключением: диспетчер превратит его в ответ 400.</p>
 *
 * @since 1.0
 */
public final class PhoneFormatter implements Formatter<Phone> {

    /**
     * Код страны.
     */
    private final String country;

    /**
     * Основной конструктор.
     * @param country Код страны
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public PhoneFormatter(final @NonNull String country) {
        this.country = Objects.requireNonNull(country, "код страны не задан");
    }

    @Override
    public Phone parse(final String text, final Locale locale) throws ParseException {
        final String digits = text.replaceAll("\\D", "");
        final Phone phone;
        if (digits.length() == 10) {
            phone = new Phone(String.format("%s%s", this.country, digits));
        } else if (digits.length() == 11) {
            phone = new Phone(String.format("%s%s", this.country, digits.substring(1)));
        } else {
            throw new ParseException(
                String.format("в номере «%s» не десять и не одиннадцать цифр", text), 0
            );
        }
        return phone;
    }

    @Override
    public String print(final Phone phone, final Locale locale) {
        return String.format("+%s", phone.digits());
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.dto;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Телефон: строка запроса, разобранная форматтером.
 *
 * <p>Как и {@link Login}, тип нужен ради того, чтобы преобразованию было
 * куда вести. Строку «8 (912) 345-67-89» разбирает не конвертер, а
 * {@code PhoneFormatter}: ему нужна страна, а её сообщает аннотация на
 * параметре.</p>
 *
 * @param digits Номер цифрами, начиная с кода страны
 * @since 1.0
 */
public record Phone(@NonNull String digits) {

    /**
     * Компактный конструктор: телефон без цифр не бывает.
     * @param digits Номер цифрами, начиная с кода страны
     */
    public Phone {
        Objects.requireNonNull(digits, "в телефоне нет цифр");
    }
}

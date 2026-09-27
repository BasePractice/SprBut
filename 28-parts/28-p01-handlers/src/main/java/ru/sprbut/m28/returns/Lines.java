/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.returns;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Ответ построчно.
 *
 * <p>Тип возвращаемого значения, который не знает ни один штатный
 * обработчик. Отдать его клиенту умеет только {@link LinesReturnValueHandler}.</p>
 *
 * @param items Строки ответа
 * @since 1.0
 */
public record Lines(@NonNull List<String> items) {

    /**
     * Компактный конструктор: без строк ответа не бывает.
     * @param items Строки ответа
     */
    public Lines {
        Objects.requireNonNull(items, "у ответа нет строк");
    }
}

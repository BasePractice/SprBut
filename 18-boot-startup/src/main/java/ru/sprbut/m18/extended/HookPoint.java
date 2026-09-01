/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m18.extended;

/**
 * Точка расширения при запуске: когда вызывается и что к этому моменту готово.
 *
 * <p>Поле {@code ready} — самое важное из четырёх. Ответ на вопрос «куда вешать
 * свой код» выводится не из названия события, а из того, что к моменту события
 * уже существует.</p>
 *
 * @param order номер шага в последовательности запуска
 * @param name  название события или интерфейса
 * @param ready что к этому моменту готово
 * @param usage типичное применение
 * @since 1.0
 */
public record HookPoint(int order, String name, String ready, String usage) {
}

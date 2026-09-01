/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21;

/**
 * Разбор одной поломки контекста: что произошло и что с этим делать.
 *
 * <p>Ровно та пара вопросов, на которую отвечает {@code FailureAnalyzer} в Spring Boot:
 * стектрейс на 200 строк сжимается до одного предложения о причине
 * и одного предложения о лечении.</p>
 *
 * @since 1.0
 */
public interface Diagnosis {

    /**
     * Что сломалось — одно предложение без точки в конце.
     * @return Описание поломки
     */
    String summary();

    /**
     * Как чинить — одно предложение без точки в конце.
     * @return Описание лечения
     */
    String remedy();
}

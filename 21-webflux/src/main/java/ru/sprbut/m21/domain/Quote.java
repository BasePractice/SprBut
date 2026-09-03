/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.domain;

/**
 * Цитата — элемент потока.
 *
 * <p>Тип нужен не ради данных, а ради кодирования: {@code Flux<String>}
 * WebFlux пишет как последовательность символов при любом типе содержимого,
 * потому что {@code CharSequence} кодируется раньше, чем до дела доходит
 * Jackson. Массивом JSON поток становится только тогда, когда его элемент —
 * объект.</p>
 *
 * @param text Текст цитаты
 * @since 1.0
 */
public record Quote(String text) {
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// поля объявлены по-ломбоковски: @Value сам делает их private final,
// а имена вроде fullName — предмет разговора о маппинге
// @checkstyle VisibilityModifierCheck disable
// @checkstyle MemberNameCheck disable
// @checkstyle EmptyLineSeparatorCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m10.lombok;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * Lombok: {@code @Value} — неизменяемый аналог {@code @Data}.
 *
 * <p>Все поля становятся {@code private final}, класс — {@code final},
 * сеттеров нет, зато есть конструктор со всеми аргументами.
 * {@code @Builder} добавляет сборку по частям — вместе получается ровно то,
 * что в модуле 02 писалось руками на 130 строк.</p>
 *
 * @since 1.0
 */
@Value
@Builder(toBuilder = true)
public class CustomerDto {

    /**
     * Идентификатор.
     */
    String id;
    /**
     * Имя.
     */
    String fullName;
    /**
     * Возраст.
     */
    int age;
    /**
     * Баланс.
     */
    BigDecimal balance;
    /**
     * Статус.
     */
    String status;
}

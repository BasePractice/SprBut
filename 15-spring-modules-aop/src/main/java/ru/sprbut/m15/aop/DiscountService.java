/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.aop;

import java.math.BigDecimal;

/**
 * Слайд 122: «JDK dynamic proxy — если есть интерфейс».
 *
 * <p>Практическое следствие: бин с интерфейсом попадает в контекст как JDK-прокси,
 * и получить его по классу реализации уже нельзя — только по интерфейсу.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface DiscountService {

    /**
     * Вычисление.
     * @param amount Сумма
     * @return Вычисление
     */
    BigDecimal calculate(BigDecimal amount);

    /**
     * Имя.
     * @return Имя
     */
    String name();
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.injection;

import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;
import java.math.BigDecimal;

/**
 * Слайды 91–92: «Через конструктор» и «Конструктор предпочтителен:
 * {@code final}, обязательность».
 *
 * <p>Три конкретных преимущества, каждое из которых проверяется тестом:
 * <ul>
 * <li>поля {@code final} — объект неизменяем и потокобезопасен по построению;</li>
 * <li>зависимость <b>обязательна</b>: объект нельзя создать в невалидном
 * состоянии, потому что другого конструктора нет;</li>
 * <li>класс тестируется <b>без контейнера</b> — достаточно обычного {@code new}.</li>
 * </ul>
 * Начиная со Spring 4.3 {@code @Autowired} на единственном конструкторе
 * не нужен: контейнер и так возьмёт его.</p>
 *
 * @since 1.0
 */
@Component
public class ConstructorInjected {

    /**
     * Сервис.
     */
    private final TaxService taxService;
    /**
     * Сервис.
     */

    private final DiscountService discountService;

    /**
     * Основной конструктор.
     * @param taxService Сервис
     * @param discountService Сервис
     */
    public ConstructorInjected(final TaxService taxService, final DiscountService discountService) {
        this.taxService = taxService;
        this.discountService = discountService;
    }

    /**
     * Итоговая сумма.
     * @param net Сумма без налога
     * @param vip Признак привилегированного клиента
     * @return Итоговая сумма
     */
    public BigDecimal total(final BigDecimal net, final boolean vip) {
        return this.discountService.apply(this.taxService.applyVat(net), vip);
    }
}

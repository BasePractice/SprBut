/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;
import java.math.BigDecimal;

/**
 * Слайд 93: «Внедрение в поле мешает тестам без контейнера».
 *
 * <p>Выглядит короче всего — и именно поэтому встречается чаще всего.
 * Проблемы начинаются за пределами happy path:
 * <ul>
 * <li>класс <b>невозможно</b> собрать обычным {@code new}: поля останутся
 * null, а подставить их можно только рефлексией или контейнером;</li>
 * <li>зависимости не видны в API класса — их приходится искать глазами по полям;</li>
 * <li>ничто не мешает добавить десятую зависимость, поэтому класс тихо
 * разрастается: конструктор на десять параметров хотя бы выглядит плохо;</li>
 * <li>поля не могут быть {@code final}.</li>
 * </ul>
 * Spring на такое внедрение выдаёт предупреждение «Field injection is not recommended».</p>
 *
 * @since 1.0
 */
@Component
public class FieldInjected {

    /**
     * Сервис.
     */
    @Autowired
    private TaxService taxService;

    /**
     * Сервис.
     */
    @Autowired
    private DiscountService discountService;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public FieldInjected() {
        // нечего инициализировать
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

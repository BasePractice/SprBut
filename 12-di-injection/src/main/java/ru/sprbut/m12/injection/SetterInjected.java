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
 * Слайд 91: «Через сеттер».
 *
 * <p>Единственный случай, где сеттер действительно уместен — <b>необязательная</b>
 * зависимость: {@code @Autowired(required = false)} оставит поле пустым,
 * если подходящего бина нет.</p>
 *
 * <p>Цена: поля не могут быть {@code final}, и между созданием объекта и вызовом
 * сеттера он находится в невалидном состоянии. Тест ниже это фиксирует.</p>
 *
 * @since 1.0
 */
@Component
public class SetterInjected {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SetterInjected() {
        // нечего инициализировать
    }

    /**
     * Сервис.
     */
    private TaxService taxService;
    /**
     * Сервис.
     */

    private DiscountService discountService;

    /**
     * Новое значение свойства {@code taxService}.
     * @param taxService Сервис
     */
    @Autowired
    public void setTaxService(final TaxService taxService) {
        this.taxService = taxService;
    }

    /**
     * Необязательная зависимость: без неё объект тоже работоспособен.
     * @param discountService Сервис
     */
    @Autowired(required = false)
    public void setDiscountService(final DiscountService discountService) {
        this.discountService = discountService;
    }

    /**
     * Итоговая сумма.
     * @param net Сумма без налога
     * @param vip Признак привилегированного клиента
     * @return Итоговая сумма
     */
    public BigDecimal total(final BigDecimal net, final boolean vip) {
        final BigDecimal withVat = this.taxService.applyVat(net);
        return this.discountService == null ? withVat : this.discountService.apply(withVat, vip);
    }

    /**
     * Значение свойства {@code discountService}.
     * @return Значение свойства {@code discountService}
     */
    public boolean hasDiscountService() {
        return this.discountService != null;
    }
}

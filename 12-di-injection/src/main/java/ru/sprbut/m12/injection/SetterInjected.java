/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// тема раздела — внедрение через сеттеры: параметр сеттера одноимён полю,
// именно такую пару «поле — сеттер» ищет контейнер
// @checkstyle HiddenFieldCheck disable
package ru.sprbut.m12.injection;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;

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
     * Сервис.
     */
    private TaxService taxes;

    /**
     * Сервис.
     */
    private DiscountService discounts;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SetterInjected() {
        // нечего инициализировать
    }

    /**
     * Новое значение свойства {@code taxes}.
     * @param taxes Сервис
     */
    @Autowired
    public void setTaxService(final TaxService taxes) {
        this.taxes = taxes;
    }

    /**
     * Итоговая сумма.
     * @param net Сумма без налога
     * @param vip Признак привилегированного клиента
     * @return Итоговая сумма
     */
    public BigDecimal total(final BigDecimal net, final boolean vip) {
        final BigDecimal vat = this.taxes.applyVat(net);
        final BigDecimal total;
        if (this.discounts == null) {
            total = vat;
        } else {
            total = this.discounts.apply(vat, vip);
        }
        return total;
    }

    /**
     * Значение свойства {@code discounts}.
     * @return Значение свойства {@code discounts}
     */
    public boolean hasDiscountService() {
        return this.discounts != null;
    }

    /**
     * Необязательная зависимость: без неё объект тоже работоспособен.
     * @param discounts Сервис
     */
    @Autowired(required = false)
    public void setDiscountService(final DiscountService discounts) {
        this.discounts = discounts;
    }
}

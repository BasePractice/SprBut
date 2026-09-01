/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.jakarta;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;
import java.math.BigDecimal;

/**
 * Слайд 96: «jakarta: {@code @Inject}, {@code @Named}, {@code @Resource}».
 *
 * <p>Это <b>стандарт</b> (JSR-330 / JSR-250), а не изобретение Spring. Spring его
 * поддерживает, если соответствующие библиотеки есть в classpath.
 * Разница в семантике:
 * <ul>
 * <li>{@code @Inject} — полный аналог {@code @Autowired}, ищет <b>по типу</b>.
 * Отличие: у него нет {@code required = false};</li>
 * <li>{@code @Named} — аналог {@code @Qualifier}: уточняет, какой именно бин;</li>
 * <li>{@code @Resource} — ищет сначала <b>по имени</b>, и только потом по типу.
 * Это единственная из трёх, у которой другой порядок разрешения.</li>
 * </ul>
 * Практическая ценность — переносимость: класс с {@code @Inject} работает
 * и в Spring, и в Guice, и в Micronaut (слайд 97).</p>
 *
 * @since 1.0
 */
@Named("jakartaService")
public class JakartaInjected {

    /**
     * Сервис.
     */
    private final TaxService taxService;

    /** {@code @Resource} ищет по имени поля — здесь это {@code discountService}. */
    @Resource
    private DiscountService discountService;

    /**
     * Значение {@code JakartaInjected}.
     * @param taxService Сервис
     */
    @Inject
    public JakartaInjected(final TaxService taxService) {
        this.taxService = taxService;
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

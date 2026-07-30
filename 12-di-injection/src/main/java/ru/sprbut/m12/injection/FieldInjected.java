package ru.sprbut.m12.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;

import java.math.BigDecimal;

/**
 * Слайд 93: «Внедрение в поле мешает тестам без контейнера».
 * <p>
 * Выглядит короче всего — и именно поэтому встречается чаще всего.
 * Проблемы начинаются за пределами happy path:
 * <ul>
 *   <li>класс <b>невозможно</b> собрать обычным {@code new}: поля останутся
 *       null, а подставить их можно только рефлексией или контейнером;</li>
 *   <li>зависимости не видны в API класса — их приходится искать глазами по полям;</li>
 *   <li>ничто не мешает добавить десятую зависимость, поэтому класс тихо
 *       разрастается: конструктор на десять параметров хотя бы выглядит плохо;</li>
 *   <li>поля не могут быть {@code final}.</li>
 * </ul>
 * Spring на такое внедрение выдаёт предупреждение «Field injection is not recommended».
 */
@Component
public class FieldInjected {

    @Autowired
    private TaxService taxService;

    @Autowired
    private DiscountService discountService;

    public BigDecimal total(BigDecimal net, boolean vip) {
        return discountService.apply(taxService.applyVat(net), vip);
    }
}

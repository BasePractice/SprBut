package ru.sprbut.m12.jakarta;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;

import java.math.BigDecimal;

/**
 * Слайд 96: «jakarta: {@code @Inject}, {@code @Named}, {@code @Resource}».
 * <p>
 * Это <b>стандарт</b> (JSR-330 / JSR-250), а не изобретение Spring. Spring его
 * поддерживает, если соответствующие библиотеки есть в classpath.
 * Разница в семантике:
 * <ul>
 *   <li>{@code @Inject} — полный аналог {@code @Autowired}, ищет <b>по типу</b>.
 *       Отличие: у него нет {@code required = false};</li>
 *   <li>{@code @Named} — аналог {@code @Qualifier}: уточняет, какой именно бин;</li>
 *   <li>{@code @Resource} — ищет сначала <b>по имени</b>, и только потом по типу.
 *       Это единственная из трёх, у которой другой порядок разрешения.</li>
 * </ul>
 * Практическая ценность — переносимость: класс с {@code @Inject} работает
 * и в Spring, и в Guice, и в Micronaut (слайд 97).
 */
@Named("jakartaService")
public class JakartaInjected {

    private final TaxService taxService;

    /** {@code @Resource} ищет по имени поля — здесь это {@code discountService}. */
    @Resource
    private DiscountService discountService;

    @Inject
    public JakartaInjected(TaxService taxService) {
        this.taxService = taxService;
    }

    public BigDecimal total(BigDecimal net, boolean vip) {
        return discountService.apply(taxService.applyVat(net), vip);
    }
}

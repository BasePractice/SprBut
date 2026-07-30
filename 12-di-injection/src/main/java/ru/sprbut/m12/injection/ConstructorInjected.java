package ru.sprbut.m12.injection;

import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;

import java.math.BigDecimal;

/**
 * Слайды 91–92: «Через конструктор» и «Конструктор предпочтителен:
 * {@code final}, обязательность».
 * <p>
 * Три конкретных преимущества, каждое из которых проверяется тестом:
 * <ul>
 *   <li>поля {@code final} — объект неизменяем и потокобезопасен по построению;</li>
 *   <li>зависимость <b>обязательна</b>: объект нельзя создать в невалидном
 *       состоянии, потому что другого конструктора нет;</li>
 *   <li>класс тестируется <b>без контейнера</b> — достаточно обычного {@code new}.</li>
 * </ul>
 * Начиная со Spring 4.3 {@code @Autowired} на единственном конструкторе
 * не нужен: контейнер и так возьмёт его.
 */
@Component
public class ConstructorInjected {

    private final TaxService taxService;
    private final DiscountService discountService;

    public ConstructorInjected(TaxService taxService, DiscountService discountService) {
        this.taxService = taxService;
        this.discountService = discountService;
    }

    public BigDecimal total(BigDecimal net, boolean vip) {
        return discountService.apply(taxService.applyVat(net), vip);
    }
}

package ru.sprbut.m12.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.sprbut.m12.domain.DiscountService;
import ru.sprbut.m12.domain.TaxService;

import java.math.BigDecimal;

/**
 * Слайд 91: «Через сеттер».
 * <p>
 * Единственный случай, где сеттер действительно уместен — <b>необязательная</b>
 * зависимость: {@code @Autowired(required = false)} оставит поле пустым,
 * если подходящего бина нет.
 * <p>
 * Цена: поля не могут быть {@code final}, и между созданием объекта и вызовом
 * сеттера он находится в невалидном состоянии. Тест ниже это фиксирует.
 */
@Component
public class SetterInjected {

    private TaxService taxService;
    private DiscountService discountService;

    @Autowired
    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    /** Необязательная зависимость: без неё объект тоже работоспособен. */
    @Autowired(required = false)
    public void setDiscountService(DiscountService discountService) {
        this.discountService = discountService;
    }

    public BigDecimal total(BigDecimal net, boolean vip) {
        BigDecimal withVat = taxService.applyVat(net);
        return discountService == null ? withVat : discountService.apply(withVat, vip);
    }

    public boolean hasDiscountService() {
        return discountService != null;
    }
}

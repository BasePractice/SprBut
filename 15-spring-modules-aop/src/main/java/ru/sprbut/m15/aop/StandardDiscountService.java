package ru.sprbut.m15.aop;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Реализация {@link DiscountService}. Именно её Spring обернёт JDK-прокси. */
@Service
public class StandardDiscountService implements DiscountService {

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.95"));
    }

    @Override
    public String name() {
        return "standard";
    }
}

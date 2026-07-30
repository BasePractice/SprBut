package ru.sprbut.m12.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Вторая зависимость — чтобы у сервиса их было больше одной. */
@Component
public class DiscountService {

    public BigDecimal apply(BigDecimal amount, boolean vip) {
        return vip
                ? amount.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP)
                : amount;
    }
}

package ru.sprbut.m11.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Вторая зависимость сервиса — чтобы граф был не тривиальным. */
public class PriceCalculator {

    private final BigDecimal vatRate;

    public PriceCalculator(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal withVat(BigDecimal net) {
        return net.multiply(BigDecimal.ONE.add(vatRate)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal vatRate() {
        return vatRate;
    }
}

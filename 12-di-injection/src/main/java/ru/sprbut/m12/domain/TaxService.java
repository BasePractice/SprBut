package ru.sprbut.m12.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Простая зависимость, которую будут внедрять тремя способами. */
@Component
public class TaxService {

    public BigDecimal applyVat(BigDecimal net) {
        return net.multiply(new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);
    }
}

package ru.sprbut.m21.ambiguous;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Потребитель, который называет нужную реализацию по имени.
 * <p>
 * {@code @Qualifier} работает на стороне точки внедрения — в отличие от
 * {@code @Primary}, выбор делает тот, кто зависимость получает, а не тот,
 * кто её объявляет.
 */
@Service
public final class EconomyDelivery {

    private final Shipper shipper;

    public EconomyDelivery(@Qualifier("economy") Shipper shipper) {
        this.shipper = shipper;
    }

    /**
     * Срок доставки экономного канала.
     */
    public int promise() {
        return this.shipper.days();
    }
}

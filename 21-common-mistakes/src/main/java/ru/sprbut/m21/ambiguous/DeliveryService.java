package ru.sprbut.m21.ambiguous;

import org.springframework.stereotype.Service;

/**
 * Потребитель, из-за которого неоднозначность становится ошибкой.
 * <p>
 * Просит {@link Shipper} «вообще», не уточняя какой.
 */
@Service
public final class DeliveryService {

    private final Shipper shipper;

    public DeliveryService(Shipper shipper) {
        this.shipper = shipper;
    }

    /**
     * Срок доставки, обещанный внедрённой службой.
     */
    public int promise() {
        return this.shipper.days();
    }
}

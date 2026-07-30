package ru.sprbut.m21.missing;

/**
 * Реализация платёжного канала, которая чинит {@link MissingBeanConfig}.
 * <p>
 * Класс существовал всё это время — не хватало только строчки, объявляющей его бином.
 */
public final class CardGateway implements PaymentGateway {

    @Override
    public String channel() {
        return "card";
    }
}

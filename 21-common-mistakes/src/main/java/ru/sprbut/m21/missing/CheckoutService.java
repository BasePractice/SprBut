package ru.sprbut.m21.missing;

import org.springframework.stereotype.Service;

/**
 * Слайд «Типичные ошибки»: {@code NoSuchBeanDefinitionException} — бин не найден.
 * <p>
 * Сервис требует {@link PaymentGateway} через конструктор. Пока в контексте нет
 * ни одной реализации, контейнер падает на этапе создания бина, а не при вызове
 * метода — это и есть главное отличие DI от {@code new}: ошибка проводки
 * обнаруживается на старте, а не в проде под нагрузкой.
 */
@Service
public final class CheckoutService {

    private final PaymentGateway gateway;

    public CheckoutService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Проводит оплату через внедрённый канал.
     */
    public String pay() {
        return "оплата через " + this.gateway.channel();
    }
}

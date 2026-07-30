package ru.sprbut.m11.step2;

import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;

import java.math.BigDecimal;

/**
 * Слайд 86: «пример — ручное управление».
 * <p>
 * Тот же сервис, но зависимости <b>приходят снаружи</b>, через конструктор.
 * Инверсия управления уже произошла: решение «какой отправитель использовать»
 * принимает не сервис, а тот, кто его создаёт.
 * <p>
 * Что изменилось:
 * <ul>
 *   <li>реализацию можно подменить — в тесте достаточно передать другую;</li>
 *   <li>зависимости видны в сигнатуре: конструктор — это честный список
 *       того, без чего объект не работает;</li>
 *   <li>поля можно сделать {@code final} — объект неизменяем и заведомо валиден.</li>
 * </ul>
 * Осталась одна проблема: кто-то должен всё это собрать — см. {@link ObjectFactory}.
 */
public class ManualOrderService {

    private final NotificationSender sender;
    private final PriceCalculator calculator;

    public ManualOrderService(NotificationSender sender, PriceCalculator calculator) {
        this.sender = sender;
        this.calculator = calculator;
    }

    public BigDecimal placeOrder(String customer, BigDecimal netAmount) {
        BigDecimal total = calculator.withVat(netAmount);
        sender.send(customer, "Заказ на сумму " + total);
        return total;
    }

    public String usedChannel() {
        return sender.channel();
    }
}

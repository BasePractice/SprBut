/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.step2;

import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import java.math.BigDecimal;

/**
 * Слайд 86: «пример — ручное управление».
 *
 * <p>Тот же сервис, но зависимости <b>приходят снаружи</b>, через конструктор.
 * Инверсия управления уже произошла: решение «какой отправитель использовать»
 * принимает не сервис, а тот, кто его создаёт.</p>
 *
 * <p>Что изменилось:
 * <ul>
 * <li>реализацию можно подменить — в тесте достаточно передать другую;</li>
 * <li>зависимости видны в сигнатуре: конструктор — это честный список
 * того, без чего объект не работает;</li>
 * <li>поля можно сделать {@code final} — объект неизменяем и заведомо валиден.</li>
 * </ul>
 * Осталась одна проблема: кто-то должен всё это собрать — см. {@link ObjectFactory}.</p>
 *
 * @since 1.0
 */
public class ManualOrderService {

    /**
     * Отправитель.
     */
    private final NotificationSender sender;
    /**
     * Калькулятор.
     */
    private final PriceCalculator calculator;

    /**
     * Основной конструктор.
     * @param sender Отправитель
     * @param calculator Калькулятор
     */
    public ManualOrderService(final NotificationSender sender, final PriceCalculator calculator) {
        this.sender = sender;
        this.calculator = calculator;
    }

    /**
     * Порядок.
     * @param customer Клиент
     * @param netAmount Сумма
     * @return Порядок
     */
    public BigDecimal placeOrder(final String customer, final BigDecimal netAmount) {
        final BigDecimal total = this.calculator.withVat(netAmount);
        this.sender.send(customer, "Заказ на сумму " + total);
        return total;
    }

    /**
     * Использованный канал.
     * @return Использованный канал
     */
    public String usedChannel() {
        return this.sender.channel();
    }
}

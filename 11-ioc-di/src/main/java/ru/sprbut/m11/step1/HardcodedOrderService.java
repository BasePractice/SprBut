/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.step1;

import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.PriceCalculator;
import java.math.BigDecimal;

/**
 * Слайд 86: «пример — без зависимостей».
 *
 * <p>Точнее — <b>зависимости есть, но управления ими нет</b>. Сервис создаёт их
 * сам, через {@code new}, и это оборачивается четырьмя проблемами сразу:
 * <ul>
 * <li>реализацию нельзя подменить — ни в тесте, ни в другом окружении;</li>
 * <li>конфигурация зашита в код: ставка НДС здесь литерал;</li>
 * <li>каждый экземпляр сервиса плодит свои копии зависимостей;</li>
 * <li>чтобы узнать, что нужно сервису, приходится читать его тело —
 * сигнатура об этом молчит.</li>
 * </ul>
 * Инверсии управления здесь нет: объект сам решает, с чем ему работать.</p>
 *
 * @since 1.0
 */
public class HardcodedOrderService {
    // Зависимости создаются внутри. Снаружи на них никак не повлиять.
    /**
     * Отправитель.
     */
    private final EmailSender sender = new EmailSender();

    /**
     * Калькулятор.
     */
    private final PriceCalculator calculator = new PriceCalculator(new BigDecimal("0.20"));

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public HardcodedOrderService() {
        // нечего инициализировать
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
     * Единственный способ хоть что-то проверить в тесте — добавить геттер
     * в продакшн-код специально ради теста. Это уже симптом.
     * @return Единственный способ хоть что-то проверить в тесте — добавить геттер в продакшн-код специально ради теста. Это уже симптом
     */
    public EmailSender senderForTests() {
        return this.sender;
    }
}

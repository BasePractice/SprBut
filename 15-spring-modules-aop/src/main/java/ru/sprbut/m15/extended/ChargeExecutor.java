/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.extended;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Отдельный бин с {@code @Retryable} — <b>правильный</b> способ обойти
 * self-invocation. Вызов из {@link PaymentService} идёт в другой бин,
 * а значит через его прокси, и аспект честно срабатывает.
 *
 * <p>Никаких {@code AopContext} и самовнедрения: проблема решается не хитростью,
 * а разделением обязанностей.</p>
 *
 * @since 1.0
 */
@Service
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
public class ChargeExecutor {

    /**
     * Число вызовов.
     */
    private final AtomicInteger executions = new AtomicInteger();

    /**
     * Сколько первых вызовов должны упасть.
     */
    private volatile int failures;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ChargeExecutor() {
        // нечего инициализировать
    }

    /**
     * Число вызовов.
     * @return Число вызовов
     */
    public int executions() {
        return this.executions.get();
    }

    /**
     * Признак раннего отказа.
     * @param times Число повторов
     */
    public void failFirst(final int times) {
        this.failures = times;
    }

    /**
     * Сброс состояния.
     */
    public void reset() {
        this.executions.set(0);
        this.failures = 0;
    }

    /**
     * Значение {@code execute}.
     * @param order Номер заказа
     * @return Значение {@code execute}
     */
    @Retryable(attempts = 3)
    public String execute(final String order) {
        final int call = this.executions.incrementAndGet();
        if (call <= this.failures) {
            throw new IllegalStateException(String.format("сбой платежа №%d", call));
        }
        return String.format("оплачен %s", order);
    }
}

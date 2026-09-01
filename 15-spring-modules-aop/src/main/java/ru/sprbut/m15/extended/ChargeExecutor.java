/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.extended;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

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
public class ChargeExecutor {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ChargeExecutor() {
        // нечего инициализировать
    }

    /**
     * Число вызовов.
     */
    private final AtomicInteger executions = new AtomicInteger();
    /**
     * Значение {@code failuresBeforeSuccess}.
     */
    private volatile int failuresBeforeSuccess;

    /**
     * Значение {@code execute}.
     * @param orderId Порядок
     * @return Значение {@code execute}
     */
    @Retryable(attempts = 3)
    public String execute(final String orderId) {
        final int call = this.executions.incrementAndGet();
        if (call <= this.failuresBeforeSuccess) {
            throw new IllegalStateException("сбой платежа №" + call);
        }
        return "оплачен " + orderId;
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
        this.failuresBeforeSuccess = times;
    }

    /**
     * Сброс состояния.
     */
    public void reset() {
        this.executions.set(0);
        this.failuresBeforeSuccess = 0;
    }
}

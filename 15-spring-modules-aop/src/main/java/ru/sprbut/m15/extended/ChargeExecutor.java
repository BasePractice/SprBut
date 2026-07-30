package ru.sprbut.m15.extended;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Отдельный бин с {@code @Retryable} — <b>правильный</b> способ обойти
 * self-invocation. Вызов из {@link PaymentService} идёт в другой бин,
 * а значит через его прокси, и аспект честно срабатывает.
 * <p>
 * Никаких {@code AopContext} и самовнедрения: проблема решается не хитростью,
 * а разделением обязанностей.
 */
@Service
public class ChargeExecutor {

    private final AtomicInteger executions = new AtomicInteger();
    private volatile int failuresBeforeSuccess;

    @Retryable(attempts = 3)
    public String execute(String orderId) {
        int call = executions.incrementAndGet();
        if (call <= failuresBeforeSuccess) {
            throw new IllegalStateException("сбой платежа №" + call);
        }
        return "оплачен " + orderId;
    }

    public int executions() {
        return executions.get();
    }

    public void failFirst(int times) {
        this.failuresBeforeSuccess = times;
    }

    public void reset() {
        executions.set(0);
        failuresBeforeSuccess = 0;
    }
}

package ru.sprbut.m15.extended;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>Расширенный пример модуля 15 (часть 2).</b>
 * <p>
 * Один и тот же вызов, сделанный четырьмя способами — чтобы ограничение
 * self-invocation (слайд 124) перестало быть абстракцией.
 * <ul>
 *   <li>{@link #charge} снаружи — аспект работает;</li>
 *   <li>{@link #chargeViaThis} — вызов через {@code this}, аспект <b>не</b> работает;</li>
 *   <li>{@link #chargeViaSelf} — обход через самовнедрение;</li>
 *   <li>{@link #chargeViaAopContext} — обход через {@code AopContext}.</li>
 * </ul>
 * Правильное решение, впрочем, ни то и ни другое: метод стоит вынести
 * в отдельный бин, чтобы вызов честно шёл через прокси
 * (см. {@link ChargeExecutor}).
 * <p>
 * <b>Состояние доступно только через методы</b>, и это не стилистика.
 * CGLIB-прокси — это отдельный объект-подкласс, который делегирует вызовы
 * настоящему бину. Его собственные поля не инициализированы, поэтому чтение
 * и запись полей «через бин из контекста» попадут мимо цели. Через методы —
 * попадут куда надо, потому что вызов будет делегирован.
 */
@Service
public class PaymentService {

    private final AtomicInteger executions = new AtomicInteger();
    private volatile int failuresBeforeSuccess;

    private final ObjectProvider<PaymentService> self;
    private final ChargeExecutor executor;

    /**
     * {@code ObjectProvider} вместо прямого внедрения себя: обычная зависимость
     * на самого себя даёт циклическую зависимость (модуль 12), а провайдер
     * достаёт бин лениво — уже готовый прокси.
     */
    public PaymentService(ObjectProvider<PaymentService> self, ChargeExecutor executor) {
        this.self = self;
        this.executor = executor;
    }

    @Retryable(attempts = 3)
    public String charge(String orderId) {
        int call = executions.incrementAndGet();
        if (call <= failuresBeforeSuccess) {
            throw new IllegalStateException("сбой платежа №" + call);
        }
        return "оплачен " + orderId;
    }

    /** Вызов через {@code this} — прокси в стороне, аспект не срабатывает. */
    public String chargeViaThis(String orderId) {
        return charge(orderId);
    }

    /** Обход 1: взять себя из контейнера — то есть свой же прокси. */
    public String chargeViaSelf(String orderId) {
        return self.getObject().charge(orderId);
    }

    /** Обход 2: {@code AopContext} — требует {@code exposeProxy = true}. */
    public String chargeViaAopContext(String orderId) {
        return ((PaymentService) AopContext.currentProxy()).charge(orderId);
    }

    /** Правильное решение: вызов уходит в другой бин, то есть через его прокси. */
    public String chargeViaSeparateBean(String orderId) {
        return executor.execute(orderId);
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

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.extended;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>Расширенный пример модуля 15 (часть 2).</b>
 *
 * <p>Один и тот же вызов, сделанный четырьмя способами — чтобы ограничение
 * self-invocation (слайд 124) перестало быть абстракцией.
 * <ul>
 * <li>{@link #charge} снаружи — аспект работает;</li>
 * <li>{@link #chargeViaThis} — вызов через {@code this}, аспект <b>не</b> работает;</li>
 * <li>{@link #chargeViaSelf} — обход через самовнедрение;</li>
 * <li>{@link #chargeViaAopContext} — обход через {@code AopContext}.</li>
 * </ul>
 * Правильное решение, впрочем, ни то и ни другое: метод стоит вынести
 * в отдельный бин, чтобы вызов честно шёл через прокси
 * (см. {@link ChargeExecutor}).</p>
 *
 * <p><b>Состояние доступно только через методы</b>, и это не стилистика.
 * CGLIB-прокси — это отдельный объект-подкласс, который делегирует вызовы
 * настоящему бину. Его собственные поля не инициализированы, поэтому чтение
 * и запись полей «через бин из контекста» попадут мимо цели. Через методы —
 * попадут куда надо, потому что вызов будет делегирован.</p>
 *
 * @since 1.0
 */
@Service
public class PaymentService {

    /**
     * Число вызовов.
     */
    private final AtomicInteger executions = new AtomicInteger();
    /**
     * Значение {@code failuresBeforeSuccess}.
     */

    private volatile int failuresBeforeSuccess;

    /**
     * Ссылка на себя.
     */
    private final ObjectProvider<PaymentService> self;
    /**
     * Исполнитель.
     */

    private final ChargeExecutor executor;

    /**
     * {@code ObjectProvider} вместо прямого внедрения себя: обычная зависимость
     * на самого себя даёт циклическую зависимость (модуль 12), а провайдер
     * достаёт бин лениво — уже готовый прокси.
     * @param executor Исполнитель
     * @param self Ссылка на себя
     */
    public PaymentService(final ObjectProvider<PaymentService> self, final ChargeExecutor executor) {
        this.self = self;
        this.executor = executor;
    }

    /**
     * Списание.
     * @param orderId Порядок
     * @return Списание
     */
    @Retryable(attempts = 3)
    public String charge(final String orderId) {
        final int call = this.executions.incrementAndGet();
        if (call <= this.failuresBeforeSuccess) {
            throw new IllegalStateException("сбой платежа №" + call);
        }
        return "оплачен " + orderId;
    }

    /**
     * Вызов через {@code this} — прокси в стороне, аспект не срабатывает.
     * @param orderId Порядок
     * @return Вызов через {@code this} — прокси в стороне, аспект не срабатывает
     */
    public String chargeViaThis(final String orderId) {
        return this.charge(orderId);
    }

    /**
     * Обход 1: взять себя из контейнера — то есть свой же прокси.
     * @param orderId Порядок
     * @return Обход 1: взять себя из контейнера — то есть свой же прокси
     */
    public String chargeViaSelf(final String orderId) {
        return this.self.getObject().charge(orderId);
    }

    /**
     * Обход 2: {@code AopContext} — требует {@code exposeProxy = true}.
     * @param orderId Порядок
     * @return Обход 2: {@code AopContext} — требует {@code exposeProxy = true}
     */
    public String chargeViaAopContext(final String orderId) {
        return ((PaymentService) AopContext.currentProxy()).charge(orderId);
    }

    /**
     * Правильное решение: вызов уходит в другой бин, то есть через его прокси.
     * @param orderId Порядок
     * @return Правильное решение: вызов уходит в другой бин, то есть через его прокси
     */
    public String chargeViaSeparateBean(final String orderId) {
        return this.executor.execute(orderId);
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

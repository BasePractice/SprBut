/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Слайд 35 и СХЕМА 2: «Proxy и InvocationHandler — основа Spring AOP».
 *
 * <p>Схема вызова: <b>вызов → Proxy → InvocationHandler → цель</b>.
 * {@link Proxy#newProxyInstance} генерирует класс в runtime, и каждый вызов
 * любого его метода попадает в один-единственный обработчик. Оттуда решается,
 * что делать: вызвать цель, подменить результат, залогировать, открыть транзакцию.</p>
 *
 * <p>Ограничение, прямо объясняющее поведение Spring AOP: JDK-прокси реализует
 * <b>только интерфейсы</b>. Нет интерфейса — Spring переключается на CGLIB-подкласс
 * (слайд 122, модуль 15).</p>
 *
 * <p>Второе ограничение видно на {@code greetTwice}: вызов соседнего метода через
 * {@code this} идёт мимо прокси, и в журнале его не будет. Ровно поэтому
 * {@code @Transactional} не работает при self-invocation.</p>
 *
 * @param <T> Параметр типа
 * @since 1.0
 */
public final class LoggingProxy<T> {

    /**
     * Контракт.
     */
    private final Class<T> contract;

    /**
     * Целевой объект.
     */
    private final T target;

    /**
     * Журнал.
     */
    private final List<String> log;

    /**
     * Основной конструктор.
     * @param contract Контракт
     * @param target Целевой объект
     * @param log Журнал
     */
    public LoggingProxy(final Class<T> contract, final T target, final List<String> log) {
        this.contract = contract;
        this.target = target;
        this.log = log;
    }

    /**
     * Прокси, пишущий в журнал вход и выход каждого вызова.
     * @return Прокси, пишущий в журнал вход и выход каждого вызова
     */
    @SuppressWarnings("unchecked")
    public T proxy() {
        return (T) Proxy.newProxyInstance(
            this.contract.getClassLoader(),
            new Class<?>[]{this.contract},
            (proxy, method, args) -> {
                this.log.add("→ " + method.getName());
                try {
                    final Object result = method.invoke(this.target, args);
                    this.log.add("← " + method.getName() + " = " + result);
                    return result;
                } catch (final InvocationTargetException wrapped) {
                    this.log.add("✗ " + method.getName() + " : " + wrapped.getCause());
                    throw wrapped.getCause();
                }
            }
        );
    }
}

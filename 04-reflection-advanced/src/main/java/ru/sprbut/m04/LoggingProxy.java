package ru.sprbut.m04;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Слайд 35 и СХЕМА 2: «Proxy и InvocationHandler — основа Spring AOP».
 * <p>
 * Схема вызова: <b>вызов → Proxy → InvocationHandler → цель</b>.
 * {@link Proxy#newProxyInstance} генерирует класс в runtime, и каждый вызов
 * любого его метода попадает в один-единственный обработчик. Оттуда решается,
 * что делать: вызвать цель, подменить результат, залогировать, открыть транзакцию.
 * <p>
 * Ограничение, прямо объясняющее поведение Spring AOP: JDK-прокси реализует
 * <b>только интерфейсы</b>. Нет интерфейса — Spring переключается на CGLIB-подкласс
 * (слайд 122, модуль 15).
 * <p>
 * Второе ограничение видно на {@code greetTwice}: вызов соседнего метода через
 * {@code this} идёт мимо прокси, и в журнале его не будет. Ровно поэтому
 * {@code @Transactional} не работает при self-invocation.
 */
public final class LoggingProxy<T> {

    private final Class<T> contract;

    private final T target;

    private final List<String> log;

    public LoggingProxy(Class<T> contract, T target, List<String> log) {
        this.contract = contract;
        this.target = target;
        this.log = log;
    }

    /**
     * Прокси, пишущий в журнал вход и выход каждого вызова.
     */
    @SuppressWarnings("unchecked")
    public T proxy() {
        return (T) Proxy.newProxyInstance(
            this.contract.getClassLoader(),
            new Class<?>[]{this.contract},
            (proxy, method, args) -> {
                this.log.add("→ " + method.getName());
                try {
                    Object result = method.invoke(this.target, args);
                    this.log.add("← " + method.getName() + " = " + result);
                    return result;
                } catch (InvocationTargetException wrapped) {
                    this.log.add("✗ " + method.getName() + " : " + wrapped.getCause());
                    throw wrapped.getCause();
                }
            }
        );
    }
}

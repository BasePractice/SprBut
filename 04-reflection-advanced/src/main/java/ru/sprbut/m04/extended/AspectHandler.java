/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Единственная точка, через которую проходят все вызовы прокси.
 *
 * <p>Порядок аспектов здесь — часть контракта, а не случайность: заглушка
 * отменяет вызов целиком, кэш стоит выше повторов (незачем повторять то,
 * что уже посчитано), замер времени охватывает все попытки.</p>
 *
 * <p>Методы {@code Object} — {@code equals}, {@code hashCode}, {@code toString} —
 * проксировать нельзя: иначе прокси станет непригоден для отладки и для
 * помещения в коллекции.</p>
 *
 * @since 1.0
 */
public final class AspectHandler implements InvocationHandler {

    /**
     * Целевой объект.
     */
    private final Object target;

    /**
     * Журнал.
     */
    private final Journal journal;

    /**
     * Значение {@code handles}.
     */
    private final Map<Method, MethodHandle> handles;

    /**
     * Значение {@code results}.
     */
    private final Map<String, Object> results;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     * @param journal Журнал
     */
    public AspectHandler(final Object target, final Journal journal) {
        this.target = target;
        this.journal = journal;
        this.handles = new ConcurrentHashMap<>();
        this.results = new ConcurrentHashMap<>();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {

            return method.invoke(this.target, args);
        }
        final Method implementation = new TargetMethod(this.target, method).method();
        final Stubbed stubbed = implementation.getAnnotation(Stubbed.class);
        if (stubbed != null) {
            this.journal.record("stub " + method.getName());
            return stubbed.value();
        }
        if (implementation.isAnnotationPresent(Cached.class)) {

            return this.cached(method, implementation, args);
        }
        return this.timed(method, implementation, args);
    }

    private Object cached(final Method method, final Method implementation, final Object[] args) throws Throwable {
        final String key = method.getName() + Arrays.toString(args);
        final Object stored = this.results.get(key);
        if (stored != null) {
            this.journal.record("cache-hit " + method.getName());
            return stored;
        }
        final Object computed = this.timed(method, implementation, args);
        if (computed != null) {
            this.results.put(key, computed);
        }
        this.journal.record("cache-miss " + method.getName());
        return computed;
    }

    private Object timed(final Method method, final Method implementation, final Object[] args) throws Throwable {
        final boolean measured = implementation.isAnnotationPresent(Timed.class);
        final long started = measured ? System.nanoTime() : 0L;
        try {
            final Retry retry = implementation.getAnnotation(Retry.class);
            if (retry == null) {
                return this.call(method, args);
            }
            return this.repeated(method, args, retry.attempts());
        } finally {
            if (measured) {
                this.journal.record(
                    String.format("timed %s %sns", method.getName(), (System.nanoTime() - started))
                );
            }
        }
    }

    private Object repeated(final Method method, final Object[] args, final int attempts) throws Throwable {
        Throwable last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                final Object result = this.call(method, args);
                if (attempt > 1) {
                    this.journal.record("retry-success " + method.getName() + " попытка " + attempt);
                }
                return result;
            } catch (final Throwable failure) {
                last = failure;
                this.journal.record("retry-fail " + method.getName() + " попытка " + attempt);
            }
        }
        this.journal.record("retry-exhausted " + method.getName());
        throw last;
    }

    private Object call(final Method method, final Object[] args) throws Throwable {
        final MethodHandle handle = this.handles.computeIfAbsent(
            method, each -> new TargetMethod(this.target, each).handle()
        );
        final Object[] withReceiver = new Object[(args == null ? 0 : args.length) + 1];
        withReceiver[0] = this.target;
        if (args != null) {
            System.arraycopy(args, 0, withReceiver, 1, args.length);
        }
        try {
            return handle.invokeWithArguments(withReceiver);
        } catch (final InvocationTargetException wrapped) {
            throw wrapped.getCause();
        }
    }
}

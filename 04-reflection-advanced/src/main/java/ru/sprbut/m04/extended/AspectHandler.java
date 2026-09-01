/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// контракт InvocationHandler: invoke объявляет throws Throwable и обязан
// пропускать через себя любое исключение цели, поэтому и весь внутренний
// конвейер обработки объявлен так же
// @checkstyle IllegalThrowsCheck disable
// @checkstyle IllegalCatchCheck disable
// массив аргументов приходит от InvocationHandler и передаётся дальше
// как есть: varargs здесь превратил бы один вызов в упаковку в новый массив
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
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {
        final Object result;
        if (method.getDeclaringClass() == Object.class) {
            result = method.invoke(this.target, args);
        } else {
            result = this.aspected(method, args);
        }
        return result;
    }

    // разбор аннотаций цели: заглушка, кэш или измерение времени
    @SuppressWarnings("PMD.UseVarargs")
    private Object aspected(final Method method, final Object[] args) throws Throwable {
        final Method impl = new TargetMethod(this.target, method).method();
        final Stubbed stubbed = impl.getAnnotation(Stubbed.class);
        final Object result;
        if (stubbed == null) {
            if (impl.isAnnotationPresent(Cached.class)) {
                result = this.cached(method, impl, args);
            } else {
                result = this.timed(method, impl, args);
            }
        } else {
            this.journal.record(String.format("stub %s", method.getName()));
            result = stubbed.value();
        }
        return result;
    }

    @SuppressWarnings("PMD.UseVarargs")
    private Object cached(final Method method, final Method impl, final Object[] args)
        throws Throwable {
        final String key = String.format("%s%s", method.getName(), Arrays.toString(args));
        final Object stored = this.results.get(key);
        final Object result;
        if (stored == null) {
            result = this.timed(method, impl, args);
            if (result != null) {
                this.results.put(key, result);
            }
            this.journal.record(String.format("cache-miss %s", method.getName()));
        } else {
            this.journal.record(String.format("cache-hit %s", method.getName()));
            result = stored;
        }
        return result;
    }

    @SuppressWarnings("PMD.UseVarargs")
    private Object timed(final Method method, final Method impl, final Object[] args)
        throws Throwable {
        final boolean measured = impl.isAnnotationPresent(Timed.class);
        final long started;
        if (measured) {
            started = System.nanoTime();
        } else {
            started = 0L;
        }
        final Object result;
        try {
            final Retry retry = impl.getAnnotation(Retry.class);
            if (retry == null) {
                result = this.call(method, args);
            } else {
                result = this.repeated(method, args, retry.attempts());
            }
        } finally {
            if (measured) {
                this.journal.record(
                    String.format("timed %s %sns", method.getName(), System.nanoTime() - started)
                );
            }
        }
        return result;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Object repeated(final Method method, final Object[] args, final int attempts)
        throws Throwable {
        Throwable last = null;
        Object result = null;
        int attempt = 0;
        while (result == null && attempt < attempts) {
            attempt += 1;
            try {
                result = this.call(method, args);
                if (attempt > 1) {
                    this.journal.record(
                        String.format("retry-success %s попытка %d", method.getName(), attempt)
                    );
                }
            } catch (final Throwable failure) {
                last = failure;
                this.journal.record(
                    String.format("retry-fail %s попытка %d", method.getName(), attempt)
                );
            }
        }
        if (result == null) {
            this.journal.record(String.format("retry-exhausted %s", method.getName()));
            throw last;
        }
        return result;
    }

    @SuppressWarnings("PMD.UseVarargs")
    private Object call(final Method method, final Object[] args) throws Throwable {
        final MethodHandle handle = this.handles.computeIfAbsent(
            method, each -> new TargetMethod(this.target, each).handle()
        );
        final int count;
        if (args == null) {
            count = 0;
        } else {
            count = args.length;
        }
        final Object[] passed = new Object[count + 1];
        passed[0] = this.target;
        if (args != null) {
            System.arraycopy(args, 0, passed, 1, args.length);
        }
        final Object result;
        try {
            result = handle.invokeWithArguments(passed);
        } catch (final InvocationTargetException wrapped) {
            throw wrapped.getCause();
        }
        return result;
    }
}

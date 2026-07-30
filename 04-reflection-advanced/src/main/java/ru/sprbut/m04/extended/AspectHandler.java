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
 * <p>
 * Порядок аспектов здесь — часть контракта, а не случайность: заглушка
 * отменяет вызов целиком, кэш стоит выше повторов (незачем повторять то,
 * что уже посчитано), замер времени охватывает все попытки.
 * <p>
 * Методы {@code Object} — {@code equals}, {@code hashCode}, {@code toString} —
 * проксировать нельзя: иначе прокси станет непригоден для отладки и для
 * помещения в коллекции.
 */
public final class AspectHandler implements InvocationHandler {

    private final Object target;

    private final Journal journal;

    private final Map<Method, MethodHandle> handles;

    private final Map<String, Object> results;

    public AspectHandler(Object target, Journal journal) {
        this.target = target;
        this.journal = journal;
        this.handles = new ConcurrentHashMap<>();
        this.results = new ConcurrentHashMap<>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this.target, args);
        }
        Method implementation = new TargetMethod(this.target, method).method();
        Stubbed stubbed = implementation.getAnnotation(Stubbed.class);
        if (stubbed != null) {
            this.journal.record("stub " + method.getName());
            return stubbed.value();
        }
        if (implementation.isAnnotationPresent(Cached.class)) {
            return cached(method, implementation, args);
        }
        return timed(method, implementation, args);
    }

    private Object cached(Method method, Method implementation, Object[] args) throws Throwable {
        String key = method.getName() + Arrays.toString(args);
        Object stored = this.results.get(key);
        if (stored != null) {
            this.journal.record("cache-hit " + method.getName());
            return stored;
        }
        Object computed = timed(method, implementation, args);
        if (computed != null) {
            this.results.put(key, computed);
        }
        this.journal.record("cache-miss " + method.getName());
        return computed;
    }

    private Object timed(Method method, Method implementation, Object[] args) throws Throwable {
        boolean measured = implementation.isAnnotationPresent(Timed.class);
        long started = measured ? System.nanoTime() : 0L;
        try {
            Retry retry = implementation.getAnnotation(Retry.class);
            if (retry == null) {
                return call(method, args);
            }
            return repeated(method, args, retry.attempts());
        } finally {
            if (measured) {
                this.journal.record(
                    "timed " + method.getName() + " " + (System.nanoTime() - started) + "ns"
                );
            }
        }
    }

    private Object repeated(Method method, Object[] args, int attempts) throws Throwable {
        Throwable last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                Object result = call(method, args);
                if (attempt > 1) {
                    this.journal.record("retry-success " + method.getName() + " попытка " + attempt);
                }
                return result;
            } catch (Throwable failure) {
                last = failure;
                this.journal.record("retry-fail " + method.getName() + " попытка " + attempt);
            }
        }
        this.journal.record("retry-exhausted " + method.getName());
        throw last;
    }

    private Object call(Method method, Object[] args) throws Throwable {
        MethodHandle handle = this.handles.computeIfAbsent(
            method, each -> new TargetMethod(this.target, each).handle()
        );
        Object[] withReceiver = new Object[(args == null ? 0 : args.length) + 1];
        withReceiver[0] = this.target;
        if (args != null) {
            System.arraycopy(args, 0, withReceiver, 1, args.length);
        }
        try {
            return handle.invokeWithArguments(withReceiver);
        } catch (InvocationTargetException wrapped) {
            throw wrapped.getCause();
        }
    }
}

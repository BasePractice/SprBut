package ru.sprbut.m04.extended;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Расширенный пример модуля 04.</b>
 * <p>
 * Работающий мини-AOP на голом JDK — без Spring и без сторонних библиотек.
 * Собирает вместе всё, что перечислено на слайдах 30–36:
 * <ul>
 *   <li><b>Proxy + InvocationHandler</b> (СХЕМА 2) — точка перехвата;</li>
 *   <li><b>аннотации в runtime</b> — что именно делать с методом;</li>
 *   <li><b>MethodHandle</b> — быстрый вызов цели вместо {@code Method.invoke},
 *       хэндлы кэшируются на метод;</li>
 *   <li><b>Parameter/Executable</b> — сборка ключа кэша по аргументам;</li>
 *   <li><b>self-invocation</b> — то самое ограничение, которое потом объясняет,
 *       почему {@code @Transactional} молча не срабатывает (модуль 15).</li>
 * </ul>
 * Это и есть, по сути, устройство Spring AOP в миниатюре: класс цели не меняется,
 * поведение живёт в обёртке.
 */
public final class JdkAopFactory {

    private JdkAopFactory() {
    }

    /** Журнал того, что делали аспекты — чтобы поведение можно было проверить тестом. */
    public static final class Journal {

        private final List<String> entries = new ArrayList<>();

        public synchronized void record(String entry) {
            entries.add(entry);
        }

        public synchronized List<String> entries() {
            return List.copyOf(entries);
        }

        public synchronized long count(String prefix) {
            return entries.stream().filter(e -> e.startsWith(prefix)).count();
        }

        public synchronized void clear() {
            entries.clear();
        }
    }

    /**
     * Оборачивает цель прокси, который читает аннотации методов и применяет аспекты.
     *
     * @param iface  интерфейс — JDK-прокси умеет только их (нет интерфейса → нужен CGLIB)
     * @param target реальная реализация
     */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<T> iface, T target, Journal journal) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface.getSimpleName()
                    + " не интерфейс: JDK-прокси умеет проксировать только интерфейсы");
        }
        InvocationHandler handler = new AspectHandler(target, journal);
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, handler);
    }

    /**
     * Обработчик — единственная точка, через которую проходят все вызовы прокси.
     */
    static final class AspectHandler implements InvocationHandler {

        private final Object target;
        private final Journal journal;
        private final Map<Method, MethodHandle> handleCache = new ConcurrentHashMap<>();
        private final Map<String, Object> resultCache = new ConcurrentHashMap<>();

        AspectHandler(Object target, Journal journal) {
            this.target = target;
            this.journal = journal;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // equals/hashCode/toString проксировать нельзя — иначе прокси станет неюзабельным
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }

            Method targetMethod = resolveTargetMethod(method);

            Aspects.Stubbed stubbed = targetMethod.getAnnotation(Aspects.Stubbed.class);
            if (stubbed != null) {
                journal.record("stub " + method.getName());
                return stubbed.value();
            }

            if (targetMethod.isAnnotationPresent(Aspects.Cached.class)) {
                String key = cacheKey(method, args);
                Object cached = resultCache.get(key);
                if (cached != null) {
                    journal.record("cache-hit " + method.getName());
                    return cached;
                }
                Object computed = applyRetryAndTiming(method, targetMethod, args);
                if (computed != null) {
                    resultCache.put(key, computed);
                }
                journal.record("cache-miss " + method.getName());
                return computed;
            }

            return applyRetryAndTiming(method, targetMethod, args);
        }

        private Object applyRetryAndTiming(Method method, Method targetMethod, Object[] args) throws Throwable {
            boolean timed = targetMethod.isAnnotationPresent(Aspects.Timed.class);
            long start = timed ? System.nanoTime() : 0L;
            try {
                Aspects.Retry retry = targetMethod.getAnnotation(Aspects.Retry.class);
                if (retry == null) {
                    return callTarget(method, args);
                }
                return callWithRetry(method, args, retry.attempts());
            } finally {
                if (timed) {
                    journal.record("timed " + method.getName() + " " + (System.nanoTime() - start) + "ns");
                }
            }
        }

        private Object callWithRetry(Method method, Object[] args, int attempts) throws Throwable {
            Throwable last = null;
            for (int attempt = 1; attempt <= attempts; attempt++) {
                try {
                    Object result = callTarget(method, args);
                    if (attempt > 1) {
                        journal.record("retry-success " + method.getName() + " попытка " + attempt);
                    }
                    return result;
                } catch (Throwable e) {
                    last = e;
                    journal.record("retry-fail " + method.getName() + " попытка " + attempt);
                }
            }
            journal.record("retry-exhausted " + method.getName());
            throw last;
        }

        /**
         * Вызов цели через {@link MethodHandle}: доступ проверяется один раз,
         * при создании хэндла, а не на каждом вызове, как у {@code Method.invoke}.
         */
        private Object callTarget(Method method, Object[] args) throws Throwable {
            MethodHandle handle = handleCache.computeIfAbsent(method, this::unreflect);
            Object[] withReceiver = new Object[(args == null ? 0 : args.length) + 1];
            withReceiver[0] = target;
            if (args != null) {
                System.arraycopy(args, 0, withReceiver, 1, args.length);
            }
            try {
                return handle.invokeWithArguments(withReceiver);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private MethodHandle unreflect(Method method) {
            try {
                Method impl = target.getClass().getMethod(method.getName(), method.getParameterTypes());
                impl.setAccessible(true);
                return MethodHandles.lookup().unreflect(impl);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Не удалось получить хэндл для " + method.getName(), e);
            }
        }

        /**
         * Аннотации ищем на методе <i>реализации</i>: у интерфейса их может не быть.
         * Ровно так же поступает Spring, разбирая {@code @Transactional}.
         */
        private Method resolveTargetMethod(Method interfaceMethod) {
            try {
                return target.getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return interfaceMethod;
            }
        }

        private String cacheKey(Method method, Object[] args) {
            return method.getName() + Arrays.toString(args);
        }
    }
}

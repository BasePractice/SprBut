package ru.sprbut.m04;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Слайд 33: «Рефлексия медленнее прямого вызова».
 * <p>
 * Здесь эта фраза измеряется — а не принимается на веру. Замер намеренно
 * <b>не</b> микробенчмарк уровня JMH: цель не получить точные наносекунды,
 * а показать порядок величины и, главное, разницу между «искать каждый раз»
 * и «искать один раз, вызывать много».
 * <p>
 * Тесты на этих числах ничего не утверждают о скорости — только о том, что
 * все четыре способа дают одинаковый результат. Замеры на CI флаки по природе.
 */
public final class InvocationCost {

    private static final MethodHandle CACHED_HANDLE = cachedHandle();
    private static final Method CACHED_METHOD = cachedMethod();

    private InvocationCost() {
    }

    private static MethodHandle cachedHandle() {
        try {
            return MethodHandles.lookup().findVirtual(Target.class, "add",
                    MethodType.methodType(int.class, int.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Method cachedMethod() {
        try {
            return Target.class.getDeclaredMethod("add", int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Прямой вызов — эталон. */
    public static int direct(Target target, int iterations) {
        int acc = 0;
        for (int i = 0; i < iterations; i++) {
            acc = target.add(acc, 1);
        }
        return acc;
    }

    /**
     * Худший вариант: поиск метода на каждой итерации. Так писать нельзя,
     * но именно так выглядит наивный код «на рефлексии».
     */
    public static int reflectionWithLookup(Target target, int iterations) throws ReflectiveOperationException {
        int acc = 0;
        for (int i = 0; i < iterations; i++) {
            Method method = Target.class.getDeclaredMethod("add", int.class, int.class);
            acc = (int) method.invoke(target, acc, 1);
        }
        return acc;
    }

    /** Метод найден один раз и закэширован — минимально приемлемый вариант. */
    public static int reflectionCached(Target target, int iterations) throws ReflectiveOperationException {
        int acc = 0;
        for (int i = 0; i < iterations; i++) {
            acc = (int) CACHED_METHOD.invoke(target, acc, 1);
        }
        return acc;
    }

    /**
     * {@code MethodHandle} в {@code static final} поле: JIT видит его как константу
     * и может заинлайнить вызов почти как прямой.
     */
    public static int methodHandle(Target target, int iterations) throws Throwable {
        int acc = 0;
        for (int i = 0; i < iterations; i++) {
            acc = (int) CACHED_HANDLE.invokeExact(target, acc, 1);
        }
        return acc;
    }

    /**
     * Прогоняет все четыре способа и возвращает время в наносекундах.
     * Полезно запустить руками; в тестах используется только для проверки
     * совпадения результатов.
     */
    public static Map<String, Long> benchmark(int iterations) throws Throwable {
        Target target = new Target();
        Map<String, Long> timings = new LinkedHashMap<>();

        long start = System.nanoTime();
        direct(target, iterations);
        timings.put("direct", System.nanoTime() - start);

        start = System.nanoTime();
        methodHandle(target, iterations);
        timings.put("methodHandle", System.nanoTime() - start);

        start = System.nanoTime();
        reflectionCached(target, iterations);
        timings.put("reflectionCached", System.nanoTime() - start);

        start = System.nanoTime();
        reflectionWithLookup(target, iterations);
        timings.put("reflectionWithLookup", System.nanoTime() - start);

        return timings;
    }

    public static class Target {
        public int add(int a, int b) {
            return a + b;
        }
    }
}

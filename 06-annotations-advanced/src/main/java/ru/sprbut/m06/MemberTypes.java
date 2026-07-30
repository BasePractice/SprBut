package ru.sprbut.m06;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Слайды 50–52: «Типы элементов: примитив, String, Class, enum, также вложенная
 * аннотация и массив» и «default-значения элементов».
 * <p>
 * Список закрыт: ничего, кроме этих шести категорий (и массивов из них),
 * в аннотации положить нельзя. Причина — значения элементов должны быть
 * константами времени компиляции, чтобы попасть в class-файл.
 */
public final class MemberTypes {

    private MemberTypes() {
    }

    public enum Isolation { DEFAULT, READ_COMMITTED, SERIALIZABLE }

    /** Вложенная аннотация — тоже допустимый тип элемента. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface Retry {
        int attempts() default 1;

        long backoffMillis() default 0L;
    }

    /**
     * Аннотация со всеми допустимыми типами элементов сразу.
     * Заодно видно, что {@code default} превращает элемент в необязательный.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Operation {

        // примитив
        int timeout() default 30;

        boolean readOnly() default false;

        // String
        String name();

        // Class
        Class<? extends Throwable> rollbackFor() default RuntimeException.class;

        // enum
        Isolation isolation() default Isolation.DEFAULT;

        // вложенная аннотация
        Retry retry() default @Retry;

        // массивы всех перечисленного
        String[] tags() default {};

        Class<?>[] handles() default {};

        Isolation[] allowed() default {Isolation.DEFAULT};
    }

    /**
     * Значения по умолчанию доступны через рефлексию отдельно от значений
     * конкретного использования. На этом строятся отчёты «эффективной конфигурации».
     */
    public static Map<String, Object> defaultsOf(Class<? extends java.lang.annotation.Annotation> annotation) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Method element : annotation.getDeclaredMethods()) {
            result.put(element.getName(), describe(element.getDefaultValue()));
        }
        return result;
    }

    /** Элементы без {@code default} обязаны быть заданы при использовании. */
    public static java.util.List<String> requiredElements(
            Class<? extends java.lang.annotation.Annotation> annotation) {
        return Arrays.stream(annotation.getDeclaredMethods())
                .filter(m -> m.getDefaultValue() == null)
                .map(Method::getName)
                .sorted()
                .toList();
    }

    /** Фактические значения элементов конкретного использования аннотации. */
    public static Map<String, Object> valuesOf(java.lang.annotation.Annotation annotation) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Method element : annotation.annotationType().getDeclaredMethods()) {
            try {
                element.setAccessible(true);
                result.put(element.getName(), describe(element.invoke(annotation)));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Не прочитать элемент " + element.getName(), e);
            }
        }
        return result;
    }

    /** Приводит значение к строке так, чтобы массивы читались, а не печатались как хэш. */
    private static Object describe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Class<?> c) {
            return c.getSimpleName();
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            Object[] items = new Object[length];
            for (int i = 0; i < length; i++) {
                items[i] = describe(java.lang.reflect.Array.get(value, i));
            }
            return Arrays.toString(items);
        }
        if (value instanceof java.lang.annotation.Annotation a) {
            return "@" + a.annotationType().getSimpleName();
        }
        return value;
    }

    /** Подопытные использования. */
    @SuppressWarnings("unused")
    public static class Service {

        @Operation(name = "минимум")
        public void withDefaults() {
        }

        @Operation(
                name = "полный",
                timeout = 5,
                readOnly = true,
                rollbackFor = IllegalStateException.class,
                isolation = Isolation.SERIALIZABLE,
                retry = @Retry(attempts = 3, backoffMillis = 250),
                tags = {"critical", "billing"},
                handles = {String.class, Integer.class},
                allowed = {Isolation.READ_COMMITTED, Isolation.SERIALIZABLE}
        )
        public void withEverything() {
        }
    }
}

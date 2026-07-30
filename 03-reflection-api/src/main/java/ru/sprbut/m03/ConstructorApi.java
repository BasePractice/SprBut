package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 24 (СХЕМА 1): {@link Constructor} — создание объектов в runtime.
 * <p>
 * Это точка входа любого IoC-контейнера: чтобы создать бин, ему нужно выбрать
 * конструктор и подобрать аргументы. Логика выбора здесь — упрощённая версия
 * того, что делает {@code AutowiredAnnotationBeanPostProcessor} (модуль 12).
 */
public final class ConstructorApi {

    private ConstructorApi() {
    }

    /** Только публичные конструкторы. */
    public static List<Integer> publicConstructorArities(Class<?> type) {
        return Arrays.stream(type.getConstructors())
                .map(Constructor::getParameterCount)
                .sorted()
                .toList();
    }

    /** Все объявленные конструкторы, включая protected и private. */
    public static List<Integer> declaredConstructorArities(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors())
                .map(Constructor::getParameterCount)
                .sorted()
                .toList();
    }

    public static Optional<Constructor<?>> noArgConstructor(Class<?> type) {
        try {
            return Optional.of(type.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Конструктор с наибольшим числом параметров — «жадная» стратегия.
     * Так, например, ведёт себя Jackson при {@code ParameterNamesModule},
     * и так же Spring выбирает единственный конструктор, если он один.
     */
    public static Optional<Constructor<?>> greediestPublicConstructor(Class<?> type) {
        return Arrays.stream(type.getConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount));
    }

    /**
     * Подбор конструктора под конкретные аргументы: сравниваем количество и
     * совместимость типов с учётом автобоксинга.
     */
    public static Optional<Constructor<?>> findMatching(Class<?> type, Object... args) {
        return Arrays.stream(type.getDeclaredConstructors())
                .filter(ctor -> matches(ctor.getParameterTypes(), args))
                // при равном соответствии предпочитаем более доступный конструктор
                .min(Comparator.comparingInt(c -> Modifier.isPublic(c.getModifiers()) ? 0 : 1));
    }

    /** Создание объекта выбранным конструктором. */
    public static Object instantiate(Class<?> type, Object... args) {
        Constructor<?> ctor = findMatching(type, args).orElseThrow(() ->
                new IllegalArgumentException("Нет конструктора " + type.getSimpleName()
                        + " под аргументы " + Arrays.toString(args)));
        ctor.setAccessible(true);
        try {
            return ctor.newInstance(args);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Нельзя создать экземпляр " + type.getSimpleName()
                    + " — абстрактный класс или интерфейс", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к конструктору", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re ? re : new IllegalStateException(cause);
        }
    }

    static boolean matches(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] == null) {
                // null нельзя передать в примитивный параметр
                if (paramTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            Class<?> expected = FieldApi.boxed(paramTypes[i]);
            if (!expected.isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }
}

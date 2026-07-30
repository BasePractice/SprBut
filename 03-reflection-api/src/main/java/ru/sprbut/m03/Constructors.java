package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 24 (СХЕМА 1): {@link Constructor} — точка входа любого IoC-контейнера.
 * <p>
 * Чтобы создать бин, контейнеру нужно выбрать конструктор и подобрать аргументы.
 * Логика выбора здесь — упрощённая версия того, что делает
 * {@code AutowiredAnnotationBeanPostProcessor} из модуля 12.
 */
public final class Constructors {

    private final Class<?> type;

    public Constructors(Class<?> type) {
        this.type = type;
    }

    /**
     * Число параметров каждого публичного конструктора.
     */
    public List<Integer> publicArities() {
        return Arrays.stream(this.type.getConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * То же для всех объявленных конструкторов, включая protected и private.
     */
    public List<Integer> declaredArities() {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * Конструктор без параметров, если он есть.
     */
    public Optional<Constructor<?>> noArg() {
        try {
            return Optional.of(this.type.getDeclaredConstructor());
        } catch (NoSuchMethodException absent) {
            return Optional.empty();
        }
    }

    /**
     * Конструктор с наибольшим числом параметров — «жадная» стратегия.
     * Так ведёт себя Jackson с {@code ParameterNamesModule}.
     */
    public Optional<Constructor<?>> greediest() {
        return Arrays.stream(this.type.getConstructors())
            .max(Comparator.comparingInt(Constructor::getParameterCount));
    }

    /**
     * Конструктор, подходящий под конкретные аргументы: сравниваются количество
     * и совместимость типов с учётом автобоксинга. При равном соответствии
     * предпочитается более доступный.
     */
    public Optional<Constructor<?>> matching(Object... args) {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .filter(candidate -> fits(candidate.getParameterTypes(), args))
            .min(Comparator.comparingInt(
                candidate -> Modifier.isPublic(candidate.getModifiers()) ? 0 : 1
            ));
    }

    private boolean fits(Class<?>[] parameters, Object[] args) {
        if (parameters.length != args.length) {
            return false;
        }
        for (int index = 0; index < parameters.length; index++) {
            if (args[index] == null) {
                if (parameters[index].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!new Boxed(parameters[index]).type().isInstance(args[index])) {
                return false;
            }
        }
        return true;
    }
}

package ru.sprbut.m03.extended;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Метод, выбранный по имени и числу аргументов, вместе с их привязкой.
 * <p>
 * Поиск идёт вверх по иерархии, bridge- и синтетические методы отсеиваются —
 * иначе один и тот же метод нашёлся бы дважды. При равных кандидатах точное
 * совпадение арности предпочитается varargs-раскрытию: {@code sum(1, 2)}
 * должен попасть в {@code sum(int, int)}, а не в {@code sum(int...)}.
 */
public final class ChosenMethod {

    private final Class<?> type;

    private final String name;

    private final List<String> args;

    public ChosenMethod(Class<?> type, String name, List<String> args) {
        this.type = type;
        this.name = name;
        this.args = List.copyOf(args);
    }

    /**
     * Подходящий метод.
     */
    public Method method() {
        List<Method> candidates = new ArrayList<>();
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            for (Method candidate : current.getDeclaredMethods()) {
                if (candidate.getName().equals(this.name)
                    && !candidate.isBridge()
                    && !candidate.isSynthetic()
                    && fits(candidate)) {
                    candidates.add(candidate);
                }
            }
        }
        return candidates.stream()
            .min(
                Comparator.comparingInt((Method each) -> each.isVarArgs() ? 1 : 0)
                    .thenComparingInt(each -> new AccessRank(each).value())
            )
            .orElseThrow(() -> new IllegalArgumentException(
                "У " + this.type.getSimpleName() + " нет метода '" + this.name
                    + "' с " + this.args.size() + " аргументами"
            ));
    }

    /**
     * Аргументы, приведённые к типам параметров.
     * <p>
     * Хвост varargs упаковывается через {@link Array#newInstance}: обычным
     * {@code new} это сделать нельзя, тип элемента известен только в runtime.
     */
    public Object[] arguments() {
        Method chosen = method();
        Class<?>[] parameters = chosen.getParameterTypes();
        Object[] values = new Object[parameters.length];
        if (!chosen.isVarArgs()) {
            for (int index = 0; index < parameters.length; index++) {
                values[index] = new Argument(this.args.get(index), parameters[index]).value();
            }
            return values;
        }
        int fixed = parameters.length - 1;
        for (int index = 0; index < fixed; index++) {
            values[index] = new Argument(this.args.get(index), parameters[index]).value();
        }
        Class<?> component = parameters[fixed].getComponentType();
        Object tail = Array.newInstance(component, this.args.size() - fixed);
        for (int index = 0; index < this.args.size() - fixed; index++) {
            Array.set(tail, index, new Argument(this.args.get(fixed + index), component).value());
        }
        values[fixed] = tail;
        return values;
    }

    /**
     * Результат вызова метода на этом объекте.
     */
    public Object result(Object instance) {
        Method chosen = method();
        chosen.setAccessible(true);
        try {
            return chosen.invoke(
                Modifier.isStatic(chosen.getModifiers()) ? null : instance,
                arguments()
            );
        } catch (ReflectiveOperationException failure) {
            throw new Unwrapped(failure).cause();
        }
    }

    private boolean fits(Method candidate) {
        if (candidate.isVarArgs()) {
            return this.args.size() >= candidate.getParameterCount() - 1;
        }
        return candidate.getParameterCount() == this.args.size();
    }
}

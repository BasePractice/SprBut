package ru.sprbut.m03.extended;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Конструктор, выбранный под заданное число аргументов.
 * <p>
 * Отбор двухступенчатый: сначала по арности, затем по тому, умеет ли движок
 * построить значения нужных типов. Второй шаг важнее, чем кажется — без него
 * конструктор с параметром-коллекцией выигрывал бы у пригодного и падал позже,
 * уже на конвертации.
 */
public final class ChosenConstructor {

    private final Class<?> type;

    private final int arity;

    public ChosenConstructor(Class<?> type, int arity) {
        this.type = type;
        this.arity = arity;
    }

    /**
     * Подходящий конструктор, самый доступный из равных.
     */
    public Constructor<?> constructor() {
        if (Modifier.isAbstract(this.type.getModifiers()) || this.type.isInterface()) {
            throw new IllegalArgumentException(
                "Нельзя создать экземпляр " + this.type.getSimpleName() + ": это абстрактный тип"
            );
        }
        return Arrays.stream(this.type.getDeclaredConstructors())
            .filter(candidate -> candidate.getParameterCount() == this.arity)
            .filter(this::fillable)
            .min(Comparator.comparingInt(candidate -> new AccessRank(candidate).value()))
            .orElseThrow(() -> new IllegalArgumentException(
                "У " + this.type.getSimpleName() + " нет пригодного конструктора с "
                    + this.arity + " аргументами"
            ));
    }

    /**
     * Созданный этим конструктором объект.
     */
    public Object instance(java.util.List<String> args) {
        Constructor<?> chosen = constructor();
        Class<?>[] parameters = chosen.getParameterTypes();
        Object[] values = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            values[index] = new Argument(args.get(index), parameters[index]).value();
        }
        chosen.setAccessible(true);
        try {
            return chosen.newInstance(values);
        } catch (ReflectiveOperationException failure) {
            throw new Unwrapped(failure).cause();
        }
    }

    /**
     * Описание конструктора для отчёта.
     */
    public String text() {
        Constructor<?> chosen = constructor();
        return chosen.getDeclaringClass().getSimpleName() + "("
            + String.join(
                ", ",
                Arrays.stream(chosen.getParameterTypes()).map(Class::getSimpleName).toList()
            )
            + ")";
    }

    private boolean fillable(Constructor<?> candidate) {
        return Arrays.stream(candidate.getParameterTypes())
            .allMatch(parameter -> new Convertible(parameter).yes());
    }
}

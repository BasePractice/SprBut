package ru.sprbut.m04;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 36: «Parameter, Executable, AnnotatedElement, Array» — узлы карты,
 * про которые обычно забывают.
 * <ul>
 *   <li>{@link Executable} — общий родитель {@code Method} и {@code Constructor}.
 *       Позволяет писать один код для обоих;</li>
 *   <li>{@link Parameter} — параметр как объект: имя, тип, аннотации, флаги;</li>
 *   <li>{@link AnnotatedElement} — общий интерфейс всего, на что вешаются аннотации;</li>
 *   <li>{@link Array} — фабрика массивов, единственный способ создать массив,
 *       тип элемента которого известен только в runtime.</li>
 * </ul>
 */
public final class ExecutableApi {

    private ExecutableApi() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Inject {
        String value() default "";
    }

    /**
     * Один код для метода и конструктора — потому что оба являются {@link Executable}.
     * Так контейнер единообразно разбирает и точки внедрения через конструктор,
     * и через метод-сеттер.
     */
    public static List<String> describeParameters(Executable executable) {
        List<String> result = new ArrayList<>();
        for (Parameter parameter : executable.getParameters()) {
            String qualifier = parameter.isAnnotationPresent(Inject.class)
                    ? "@Inject(" + parameter.getAnnotation(Inject.class).value() + ") "
                    : "";
            result.add(qualifier + parameter.getType().getSimpleName() + " " + parameter.getName());
        }
        return result;
    }

    /** Имена параметров доступны, только если код собран с {@code -parameters}. */
    public static boolean parameterNamesArePresent(Executable executable) {
        return Arrays.stream(executable.getParameters()).allMatch(Parameter::isNamePresent);
    }

    /**
     * Точка внедрения — параметр, помеченный {@code @Inject}. Именно так
     * контейнер находит, что подставить, а имя из аннотации служит квалификатором.
     */
    public static List<String> injectionPoints(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .filter(p -> p.isAnnotationPresent(Inject.class))
                .map(p -> {
                    String hint = p.getAnnotation(Inject.class).value();
                    return hint.isBlank() ? p.getType().getSimpleName() : hint;
                })
                .toList();
    }

    /** Синтетические параметры компилятор добавляет сам (например, во внутренние классы). */
    public static List<String> syntheticParameters(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .filter(p -> p.isSynthetic() || p.isImplicit())
                .map(Parameter::getName)
                .toList();
    }

    /**
     * Единый разбор аннотаций для любого элемента: класса, поля, метода, параметра.
     * Все они реализуют {@link AnnotatedElement}.
     */
    public static List<String> annotationsOf(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .sorted()
                .toList();
    }

    /**
     * Массив с типом элемента, известным только в runtime. Обычный {@code new T[n]}
     * в Java невозможен из-за стирания — остаётся {@link Array#newInstance}.
     */
    public static Object createArray(Class<?> componentType, int length) {
        return Array.newInstance(componentType, length);
    }

    /** Многомерный массив создаётся тем же API. */
    public static Object createMatrix(Class<?> componentType, int rows, int columns) {
        return Array.newInstance(componentType, rows, columns);
    }

    /**
     * Универсальные чтение и запись элемента без приведения типа —
     * работают и для примитивных массивов, где {@code Object[]} неприменим.
     */
    public static void setElement(Object array, int index, Object value) {
        Array.set(array, index, value);
    }

    public static Object getElement(Object array, int index) {
        return Array.get(array, index);
    }

    public static int lengthOf(Object array) {
        return Array.getLength(array);
    }

    /** Носитель точек внедрения для тестов. */
    @SuppressWarnings("unused")
    public static class Service {

        private final String name;

        public Service(@Inject("appName") String name, @Inject int retries, boolean debug) {
            this.name = name;
        }

        public void configure(@Inject("timeout") long millis, String label) {
            // ничего не делает — важна только сигнатура
        }

        public String getName() {
            return name;
        }
    }
}

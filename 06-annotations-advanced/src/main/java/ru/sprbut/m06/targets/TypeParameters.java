package ru.sprbut.m06.targets;

import java.util.Arrays;
import java.util.List;

/**
 * Аннотации на объявлениях переменных типа: {@code class Holder<@Comparablish T>}.
 * <p>
 * Ещё одна ветка API, отдельная и от полей, и от типов их использования.
 */
public final class TypeParameters {

    private final Class<?> type;

    public TypeParameters(Class<?> type) {
        this.type = type;
    }

    /**
     * Имена аннотаций на переменной типа с указанным номером.
     */
    public List<String> names(int index) {
        return Arrays.stream(this.type.getTypeParameters()[index].getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .toList();
    }
}

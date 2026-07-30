package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Ручной подъём по иерархии — то, что приходится писать, когда
 * {@code @Inherited} не спасает.
 * <p>
 * А не спасает оно почти всегда: на аннотации может не быть {@code @Inherited},
 * искать может понадобиться на методе, а источником может оказаться интерфейс.
 * Отсюда и {@code AnnotatedElementUtils} в Spring — та же работа, только
 * с кэшированием и поддержкой композиций.
 */
public final class HierarchySearch<A extends Annotation> {

    private final Class<?> type;

    private final Class<A> annotation;

    public HierarchySearch(Class<?> type, Class<A> annotation) {
        this.type = type;
        this.annotation = annotation;
    }

    /**
     * Аннотация класса, найденная подъёмом до {@code Object}.
     */
    public Optional<A> onClass() {
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            A found = current.getDeclaredAnnotation(this.annotation);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    /**
     * Аннотация метода: сначала сам класс, затем родители, затем интерфейсы.
     * Порядок именно такой — ближайшее объявление должно побеждать.
     */
    public Optional<A> onMethod(String method, Class<?>... parameters) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            Optional<A> found = declaredOn(current, method, parameters);
            if (found.isPresent()) {
                return found;
            }
        }
        for (Class<?> contract : this.type.getInterfaces()) {
            Optional<A> found = declaredOn(contract, method, parameters);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<A> declaredOn(Class<?> owner, String method, Class<?>... parameters) {
        try {
            return Optional.ofNullable(
                owner.getDeclaredMethod(method, parameters).getDeclaredAnnotation(this.annotation)
            );
        } catch (NoSuchMethodException absent) {
            return Optional.empty();
        }
    }
}

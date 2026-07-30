package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Поиск слитой аннотации с подъёмом по иерархии классов и интерфейсов.
 * <p>
 * Нужен потому, что {@code @Inherited} умеет только суперклассы и только
 * для аннотаций типов. На интерфейсы язык не смотрит вообще — а Spring смотрит,
 * и именно поэтому {@code @Transactional} на интерфейсе репозитория работает.
 */
public final class HierarchyMerged<A extends Annotation> {

    private final Class<?> type;

    private final Class<A> target;

    public HierarchyMerged(Class<?> type, Class<A> target) {
        this.type = type;
        this.target = target;
    }

    /**
     * Найденная аннотация: сначала классы, затем интерфейсы.
     */
    public Optional<Merged> find() {
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            Optional<Merged> found = new MergedAnnotation<>(current, this.target)
                .search(current.getDeclaredAnnotations());
            if (found.isPresent()) {
                return found;
            }
        }
        for (Class<?> contract : interfaces(this.type)) {
            Optional<Merged> found = new MergedAnnotation<>(contract, this.target)
                .search(contract.getDeclaredAnnotations());
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private List<Class<?>> interfaces(Class<?> from) {
        List<Class<?>> collected = new ArrayList<>();
        for (Class<?> current = from;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Class<?> contract : current.getInterfaces()) {
                if (!collected.contains(contract)) {
                    collected.add(contract);
                    collected.addAll(interfaces(contract));
                }
            }
        }
        return collected;
    }
}

package ru.sprbut.m05;

import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Marker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Слайд 41: {@code @Inherited} — и три границы, за которые оно не работает.
 * <p>
 * Именно из-за этих ограничений Spring не полагается на {@code @Inherited},
 * а обходит иерархию сам ({@code AnnotatedElementUtils}). Модуль 06 показывает,
 * как выглядит такой обход.
 */
public final class InheritanceRules {

    private InheritanceRules() {
    }

    /** {@code getAnnotation} на классе учитывает {@code @Inherited}. */
    public static <A extends Annotation> Optional<A> onClass(Class<?> type, Class<A> annotation) {
        return Optional.ofNullable(type.getAnnotation(annotation));
    }

    /** {@code getDeclaredAnnotation} игнорирует наследование — только своё. */
    public static <A extends Annotation> Optional<A> declaredOnClass(Class<?> type, Class<A> annotation) {
        return Optional.ofNullable(type.getDeclaredAnnotation(annotation));
    }

    public static <A extends Annotation> Optional<A> onMethod(Method method, Class<A> annotation) {
        return Optional.ofNullable(method.getAnnotation(annotation));
    }

    /**
     * Ручной подъём по иерархии классов — то, что приходится писать, когда
     * {@code @Inherited} не спасает (аннотация без него, или искать надо на методе).
     */
    public static <A extends Annotation> Optional<A> searchUpHierarchy(Class<?> type, Class<A> annotation) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            A found = c.getDeclaredAnnotation(annotation);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    /**
     * Поиск аннотации метода с подъёмом по иерархии: сначала сам метод,
     * потом одноимённые методы родителей и интерфейсов.
     */
    public static <A extends Annotation> Optional<A> searchMethodUpHierarchy(
            Class<?> type, String methodName, Class<A> annotation, Class<?>... paramTypes) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            Optional<A> found = declaredOn(c, methodName, annotation, paramTypes);
            if (found.isPresent()) {
                return found;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            Optional<A> found = declaredOn(iface, methodName, annotation, paramTypes);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private static <A extends Annotation> Optional<A> declaredOn(
            Class<?> type, String methodName, Class<A> annotation, Class<?>... paramTypes) {
        try {
            return Optional.ofNullable(type.getDeclaredMethod(methodName, paramTypes)
                    .getDeclaredAnnotation(annotation));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    // --- Подопытная иерархия -------------------------------------------------

    @Audited(actor = "родитель")
    public static class Parent {

        @Audited(actor = "метод-родителя")
        public String action() {
            return "parent";
        }
    }

    /** Аннотация родителя видна здесь благодаря {@code @Inherited}. */
    public static class Child extends Parent {

        @Override
        public String action() {
            // переопределённый метод аннотацию НЕ наследует
            return "child";
        }
    }

    /** {@code @Marker} без {@code @Inherited} — на подкласс не переходит. */
    @Marker
    public static class MarkedParent {
    }

    public static class MarkedChild extends MarkedParent {
    }

    /** Аннотация на интерфейсе не переходит на реализацию — никогда. */
    @Audited(actor = "интерфейс")
    public interface AuditedContract {
        String action();
    }

    public static class ContractImpl implements AuditedContract {
        @Override
        public String action() {
            return "impl";
        }
    }
}

package ru.sprbut.m06;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

/**
 * Слайды 48–49: «Цели: ANNOTATION_TYPE, PACKAGE, TYPE_USE» и
 * «Цели: TYPE_PARAMETER, MODULE, RECORD_COMPONENT».
 * <p>
 * Это расширения {@code @Target}, появившиеся после Java 8. Ключевое из них —
 * {@code TYPE_USE}: аннотация ставится не на объявление, а на <b>использование
 * типа</b>, включая аргументы дженериков. Читается такая аннотация не с {@code Field},
 * а с {@link AnnotatedType} — отдельной ветки API.
 */
public final class ModernTargets {

    private ModernTargets() {
    }

    /** {@code ANNOTATION_TYPE} — аннотация, которую вешают на другие аннотации. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    public @interface Stereotype {
        String layer();
    }

    /** {@code TYPE_USE} — на любое использование типа, в том числе внутри дженерика. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    public @interface NonNull {
    }

    /** {@code TYPE_PARAMETER} — на объявление переменной типа {@code <T>}. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_PARAMETER)
    public @interface Comparablish {
    }

    /** {@code RECORD_COMPONENT} — на компонент record (Java 16+). */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.RECORD_COMPONENT)
    public @interface Column {
        String name();
    }

    /** Пример {@code @Stereotype} на аннотации — так строятся мета-аннотации. */
    @Stereotype(layer = "web")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Controller {
    }

    @SuppressWarnings("unused")
    public static class Holder<@Comparablish T> {

        /** Аннотация стоит на самом типе поля. */
        public @NonNull String direct;

        /** А здесь — на аргументе дженерика, что доступно только TYPE_USE. */
        public List<@NonNull String> insideGenerics;

        public List<String> plain;
    }

    /** Компоненты record можно аннотировать отдельно от полей. */
    public record UserRow(@Column(name = "user_id") String id, @Column(name = "login") String login) {
    }

    /** Аннотация на типе поля целиком. */
    public static List<String> annotationsOnFieldType(Field field) {
        return names(field.getAnnotatedType());
    }

    /**
     * Аннотации на аргументах дженерика: {@code List<@NonNull String>}.
     * Обычный {@code field.getAnnotations()} их не видит вовсе.
     */
    public static List<String> annotationsOnTypeArguments(Field field) {
        AnnotatedType annotatedType = field.getAnnotatedType();
        if (annotatedType instanceof AnnotatedParameterizedType parameterized) {
            return Arrays.stream(parameterized.getAnnotatedActualTypeArguments())
                    .flatMap(t -> names(t).stream())
                    .toList();
        }
        return List.of();
    }

    /** Аннотации на переменной типа {@code <@Comparablish T>}. */
    public static List<String> annotationsOnTypeParameter(Class<?> type, int index) {
        return Arrays.stream(type.getTypeParameters()[index].getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .toList();
    }

    /** Аннотации компонента record — отдельная ветка API. */
    public static String columnNameOf(Class<?> recordType, String component) {
        for (RecordComponent rc : recordType.getRecordComponents()) {
            if (rc.getName().equals(component)) {
                Column column = rc.getAnnotation(Column.class);
                return column == null ? null : column.name();
            }
        }
        throw new IllegalArgumentException("Нет компонента '" + component + "'");
    }

    /** Мета-аннотация читается с аннотации ровно так же, как с обычного класса. */
    public static String layerOf(Class<? extends java.lang.annotation.Annotation> annotation) {
        Stereotype stereotype = annotation.getAnnotation(Stereotype.class);
        return stereotype == null ? null : stereotype.layer();
    }

    private static List<String> names(AnnotatedType type) {
        return Arrays.stream(type.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .toList();
    }
}

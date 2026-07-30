package ru.sprbut.m10.lombok;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Точечные аннотации Lombok: то же самое, но по частям.
 * <p>
 * {@code @Data} удобен, но непрозрачен. На реальном коде чаще используют
 * отдельные аннотации — видно, что именно генерируется.
 */
public final class LombokFeatures {

    private LombokFeatures() {
    }

    /** {@code @Getter}/{@code @Setter} по отдельности, с контролем доступа. */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    public static class Partial {

        private String visible;

        /** У поля свой уровень доступа, он перекрывает общий. */
        @Getter(AccessLevel.NONE)
        private String hidden = "не виден снаружи";

        String peekHidden() {
            return hidden;
        }
    }

    /**
     * {@code @RequiredArgsConstructor} — конструктор из {@code final} полей
     * и полей с {@code @NonNull}. Это <b>основной способ</b> внедрения
     * зависимостей через конструктор в Spring-коде (модуль 12).
     */
    @RequiredArgsConstructor
    @Getter
    public static class Service {

        private final String name;
        private final int retries;
        private String mutableState = "меняется";
    }

    /**
     * {@code @ToString} и {@code @EqualsAndHashCode} с исключением полей —
     * частый приём для сущностей с ленивыми связями и паролями.
     */
    @Getter
    @RequiredArgsConstructor
    @ToString(exclude = "password")
    @EqualsAndHashCode(of = "id")
    public static class Account {

        private final String id;
        private final String login;
        private final String password;
    }

    /**
     * {@code @Builder} с {@code @Singular} — коллекция наполняется по одному
     * элементу и становится неизменяемой в собранном объекте.
     */
    @Builder
    @Getter
    public static class Order {

        private final String number;

        @Singular
        private final List<String> items;
    }

    /**
     * {@code @Accessors(fluent = true)} — аксессоры без префиксов: {@code name()}
     * вместо {@code getName()}.
     * <p>
     * Важное следствие: такой класс перестаёт быть JavaBean, и всё, что работает
     * по соглашению — {@code Introspector}, биндинг форм Spring, Jackson
     * по умолчанию — его не увидит (модуль 02).
     */
    @Accessors(fluent = true)
    @Getter
    @Setter
    public static class Fluent {

        private String name;
        private int size;
    }
}

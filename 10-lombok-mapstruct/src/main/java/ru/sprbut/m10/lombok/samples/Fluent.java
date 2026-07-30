package ru.sprbut.m10.lombok.samples;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * {@code @Accessors(fluent = true)} — аксессоры без префиксов:
 * {@code name()} вместо {@code getName()}.
 * <p>
 * Важное следствие: такой класс перестаёт быть JavaBean, и всё, что работает
 * по соглашению — {@code Introspector}, биндинг форм Spring, Jackson
 * по умолчанию — его не увидит (модуль 02).
 */
@Accessors(fluent = true)
@Getter
@Setter
public class Fluent {

    private String name;

    private int size;
}

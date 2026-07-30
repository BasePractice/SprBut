package ru.sprbut.m05.extended;

/**
 * Родитель с собственным ограничением: его поле обязано проверяться
 * наравне с полями наследника.
 */
@SuppressWarnings("unused")
public class BaseEntity {

    @NotBlank
    private final String id;

    protected BaseEntity(String id) {
        this.id = id;
    }
}

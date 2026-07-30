package ru.sprbut.m12.extended;

/**
 * Способ, которым класс получает зависимости.
 * <p>
 * Порядок объявления не случаен: он совпадает с порядком предпочтения
 * со слайдов 91–95, от лучшего к худшему.
 */
public enum Style {

    CONSTRUCTOR,
    SETTER,
    FIELD,
    SERVICE_LOCATOR,
    NONE
}

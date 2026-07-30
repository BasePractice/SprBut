package ru.sprbut.m10.lombok.samples;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code @Getter}/{@code @Setter} по отдельности, с контролем доступа.
 * <p>
 * Показывает, что Lombok — не «всё или ничего»: уровень доступа задаётся
 * и на классе, и на поле, причём поле перекрывает класс.
 */
@Getter
@Setter(AccessLevel.PROTECTED)
public class Partial {

    private String visible;

    @Getter(AccessLevel.NONE)
    private String hidden = "не виден снаружи";

    /**
     * Единственный способ прочитать поле, у которого геттер отключён.
     */
    public String peekHidden() {
        return this.hidden;
    }
}

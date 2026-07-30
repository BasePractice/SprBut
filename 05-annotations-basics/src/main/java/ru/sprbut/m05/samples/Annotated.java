package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;

/**
 * Класс с аннотациями во всех местах, перечисленных на слайде 39.
 * <p>
 * Локальная переменная помечена намеренно: её аннотация вообще не попадает
 * в class-файл, и прочитать её в runtime невозможно в принципе.
 * {@code LOCAL_VARIABLE} существует только для инструментов уровня исходников.
 */
@Marker
@Level("класс")
@SuppressWarnings("unused")
public class Annotated {

    @Level("поле")
    private String field;

    public Annotated() {
    }

    @Marker
    @Level("метод")
    public void method(String parameter) {
        @SuppressWarnings("unused")
        String local = parameter;
    }
}

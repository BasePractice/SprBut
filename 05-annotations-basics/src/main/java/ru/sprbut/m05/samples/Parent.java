package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Audited;

/**
 * Родитель с аннотацией на классе и на методе.
 * <p>
 * На классе аннотация унаследуется, на методе — нет. Разница видна
 * в {@link Child}.
 */
@Audited(actor = "родитель")
public class Parent {

    /**
     * Действие, помеченное аудитом.
     */
    @Audited(actor = "метод-родителя")
    public String action() {
        return "parent";
    }
}

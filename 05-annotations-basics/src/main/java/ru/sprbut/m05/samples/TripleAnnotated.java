package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Retentions;

/**
 * Класс, на каждом элементе которого стоят аннотации всех трёх политик хранения.
 * <p>
 * В исходниках их четыре, в байткоде — две, в runtime видна ровно одна.
 * Наглядная иллюстрация того, что «написано в коде» и «доступно рефлексии» —
 * разные множества.
 */
@Retentions.SourceLevel
@Retentions.ClassLevel
@Retentions.RuntimeLevel
@Retentions.DefaultRetention
@SuppressWarnings("unused")
public class TripleAnnotated {

    @Retentions.SourceLevel
    @Retentions.ClassLevel
    @Retentions.RuntimeLevel
    private String field;

    @Retentions.SourceLevel
    @Retentions.ClassLevel
    @Retentions.RuntimeLevel
    public void method() {
    }
}

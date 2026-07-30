package ru.sprbut.m06.targets;

import java.lang.reflect.RecordComponent;
import java.util.Optional;

/**
 * Имя колонки, объявленное на компоненте record.
 * <p>
 * Компонент record — самостоятельная сущность рефлексии: не поле, не метод
 * и не параметр конструктора, хотя порождает все три.
 */
public final class RecordColumn {

    private final Class<?> type;

    private final String component;

    public RecordColumn(Class<?> type, String component) {
        this.type = type;
        this.component = component;
    }

    /**
     * Имя колонки, если компонент помечен.
     */
    public Optional<String> name() {
        for (RecordComponent each : this.type.getRecordComponents()) {
            if (each.getName().equals(this.component)) {
                return Optional.ofNullable(each.getAnnotation(Column.class)).map(Column::name);
            }
        }
        throw new IllegalArgumentException("Нет компонента '" + this.component + "'");
    }
}

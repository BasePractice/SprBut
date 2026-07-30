package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Найденная аннотация вместе со слитыми значениями элементов.
 * <p>
 * Путь мета-аннотаций хранится не для отладки, а по делу: когда значение
 * пришло не оттуда, откуда ожидалось, единственный способ понять почему —
 * увидеть, через какую цепочку его нашли.
 *
 * @param type       тип найденной аннотации
 * @param attributes слитые значения элементов
 * @param path       цепочка мета-аннотаций от элемента до цели
 */
public record Merged(
    Class<? extends Annotation> type,
    Map<String, Object> attributes,
    List<String> path
) {

    public Merged {
        attributes = Map.copyOf(attributes);
        path = List.copyOf(path);
    }

    /**
     * Значение элемента.
     */
    public Object value(String attribute) {
        return this.attributes.get(attribute);
    }

    /**
     * Значение элемента строкой.
     */
    public String text(String attribute) {
        return String.valueOf(this.attributes.get(attribute));
    }
}

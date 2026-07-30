package ru.sprbut.m04;

import java.util.List;
import java.util.Map;

/**
 * Носитель разнообразных обобщённых объявлений: параметризованный тип,
 * вложенный дженерик, wildcard, переменная типа и обобщённый массив.
 * <p>
 * Существует ради того, чтобы у {@link GenericType} было что разбирать:
 * каждое поле здесь — отдельный род узла в дереве типов.
 */
@SuppressWarnings("unused")
public class Holder<T extends Comparable<T>> {

    public List<String> names;

    public Map<String, List<Integer>> nested;

    public List<? extends Number> covariant;

    public T typeVariable;

    public T[] genericArray;

    public String plain;

    /**
     * Метод, возвращающий обобщённый тип, — у него тоже есть своя сигнатура.
     */
    public List<T> produce() {
        return List.of();
    }
}

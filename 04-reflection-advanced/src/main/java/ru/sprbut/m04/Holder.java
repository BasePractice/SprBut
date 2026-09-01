/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.util.List;
import java.util.Map;

/**
 * Носитель разнообразных обобщённых объявлений: параметризованный тип,
 * вложенный дженерик, wildcard, переменная типа и обобщённый массив.
 *
 * <p>Существует ради того, чтобы у {@link GenericType} было что разбирать:
 * каждое поле здесь — отдельный род узла в дереве типов.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public class Holder<T extends Comparable<T>> {
    /**
     * Имена.
     */
    public List<String> names;

    /**
     * Вложенное значение.
     */
    public Map<String, List<Integer>> nested;

    /**
     * Значение {@code covariant}.
     */
    public List<? extends Number> covariant;

    /**
     * Тип.
     */
    public T typeVariable;

    /**
     * Значение {@code genericArray}.
     */
    public T[] genericArray;

    /**
     * Обычный вариант.
     */
    public String plain;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Holder() {
        // нечего инициализировать
    }

    /**
     * Метод, возвращающий обобщённый тип, — у него тоже есть своя сигнатура.
     * @return Метод, возвращающий обобщённый тип, — у него тоже есть своя сигнатура
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public List<T> produce() {
        return List.of();
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Поиск слитой аннотации с подъёмом по иерархии классов и интерфейсов.
 *
 * <p>Нужен потому, что {@code @Inherited} умеет только суперклассы и только
 * для аннотаций типов. На интерфейсы язык не смотрит вообще — а Spring смотрит,
 * и именно поэтому {@code @Transactional} на интерфейсе репозитория работает.</p>
 *
 * @param <A> Параметр типа
 * @since 1.0
 */
public final class HierarchyMerged<A extends Annotation> {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Целевой объект.
     */
    private final Class<A> target;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param target Целевой объект
     */
    public HierarchyMerged(final Class<?> type, final Class<A> target) {
        this.type = type;
        this.target = target;
    }

    /**
     * Найденная аннотация: сначала классы, затем интерфейсы.
     * @return Найденная аннотация: сначала классы, затем интерфейсы
     */
    public Optional<Merged> find() {
        Optional<Merged> found = Optional.empty();
        Class<?> current = this.type;
        while (found.isEmpty() && current != null && current != Object.class) {
            found = new MergedAnnotation<>(current, this.target)
                .search(current.getDeclaredAnnotations());
            current = current.getSuperclass();
        }
        final Iterator<Class<?>> contracts = this.interfaces(this.type).iterator();
        while (found.isEmpty() && contracts.hasNext()) {
            final Class<?> contract = contracts.next();
            found = new MergedAnnotation<>(contract, this.target)
                .search(contract.getDeclaredAnnotations());
        }
        return found;
    }

    private List<Class<?>> interfaces(final Class<?> from) {
        final List<Class<?>> collected = new ArrayList<>(0);
        Class<?> current = from;
        while (current != null && current != Object.class) {
            for (final Class<?> contract : current.getInterfaces()) {
                if (!collected.contains(contract)) {
                    collected.add(contract);
                    collected.addAll(this.interfaces(contract));
                }
            }
            current = current.getSuperclass();
        }
        return collected;
    }
}

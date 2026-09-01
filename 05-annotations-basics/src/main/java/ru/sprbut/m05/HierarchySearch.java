/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Ручной подъём по иерархии — то, что приходится писать, когда
 * {@code @Inherited} не спасает.
 *
 * <p>А не спасает оно почти всегда: на аннотации может не быть {@code @Inherited},
 * искать может понадобиться на методе, а источником может оказаться интерфейс.
 * Отсюда и {@code AnnotatedElementUtils} в Spring — та же работа, только
 * с кэшированием и поддержкой композиций.</p>
 *
 * @param <A> Тип аннотации
 * @since 1.0
 */
public final class HierarchySearch<A extends Annotation> {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Аннотация.
     */
    private final Class<A> annotation;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param annotation Аннотация
     */
    public HierarchySearch(final Class<?> type, final Class<A> annotation) {
        this.type = type;
        this.annotation = annotation;
    }

    /**
     * Аннотация класса, найденная подъёмом до {@code Object}.
     * @return Аннотация класса, найденная подъёмом до {@code Object}
     */
    public Optional<A> onClass() {
        Optional<A> found = Optional.empty();
        Class<?> current = this.type;
        while (found.isEmpty() && current != null && current != Object.class) {
            found = Optional.ofNullable(current.getDeclaredAnnotation(this.annotation));
            current = current.getSuperclass();
        }
        return found;
    }

    /**
     * Аннотация метода: сначала сам класс, затем родители, затем интерфейсы.
     * Порядок именно такой — ближайшее объявление должно побеждать.
     * @param method Метод
     * @param parameters Типы параметров
     * @return Аннотация метода: сначала сам класс, затем родители, затем интерфейсы
     */
    public Optional<A> onMethod(final String method, final Class<?>... parameters) {
        Optional<A> found = Optional.empty();
        Class<?> current = this.type;
        while (found.isEmpty() && current != null) {
            found = this.declaredOn(current, method, parameters);
            current = current.getSuperclass();
        }
        final Iterator<Class<?>> contracts = List.of(this.type.getInterfaces()).iterator();
        while (found.isEmpty() && contracts.hasNext()) {
            found = this.declaredOn(contracts.next(), method, parameters);
        }
        return found;
    }

    private Optional<A> declaredOn(final Class<?> owner, final String method,
        final Class<?>... parameters) {
        Optional<A> found;
        try {
            found = Optional.ofNullable(
                owner.getDeclaredMethod(method, parameters).getDeclaredAnnotation(this.annotation)
            );
        } catch (final NoSuchMethodException absent) {
            found = Optional.empty();
        }
        return found;
    }
}

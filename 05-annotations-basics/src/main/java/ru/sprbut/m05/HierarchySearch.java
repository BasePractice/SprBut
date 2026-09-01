/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.annotation.Annotation;
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
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            final A found = current.getDeclaredAnnotation(this.annotation);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    /**
     * Аннотация метода: сначала сам класс, затем родители, затем интерфейсы.
     * Порядок именно такой — ближайшее объявление должно побеждать.
     * @param method Метод
     * @param parameters Типы параметров
     * @return Аннотация метода: сначала сам класс, затем родители, затем интерфейсы
     */
    public Optional<A> onMethod(final String method, final Class<?>... parameters) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            final Optional<A> found = this.declaredOn(current, method, parameters);
            if (found.isPresent()) {
                return found;
            }
        }
        for (final Class<?> contract : this.type.getInterfaces()) {
            final Optional<A> found = this.declaredOn(contract, method, parameters);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<A> declaredOn(final Class<?> owner, final String method, final Class<?>... parameters) {
        try {
            return Optional.ofNullable(
                owner.getDeclaredMethod(method, parameters).getDeclaredAnnotation(this.annotation)
            );
        } catch (final NoSuchMethodException absent) {
            return Optional.empty();
        }
    }
}

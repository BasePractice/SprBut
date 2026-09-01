/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Field;

/**
 * Слайд 7: «Получить и задать значение полей, в том числе с модификатором private».
 *
 * <p>Одно поле одного объекта, доступное на чтение и запись независимо от того,
 * что написано в его модификаторах. Ключевой вызов — {@code setAccessible(true)}:
 * он снимает проверку доступа.</p>
 *
 * <p>Именно так Spring внедряет зависимости в поля с {@code @Autowired},
 * а Hibernate заполняет сущности, не требуя сеттеров. И так же рефлексия
 * пишет в {@code private final} поле, у которого никакого сеттера нет
 * и быть не может.</p>
 *
 * @since 1.0
 */
public final class ObjectField {

    /**
     * Целевой объект.
     */
    private final Object target;

    /**
     * Имя.
     */
    private final String name;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     * @param name Имя
     */
    public ObjectField(final Object target, final String name) {
        this.target = target;
        this.name = name;
    }

    /**
     * Объявление поля — найденное с подъёмом по иерархии наследования.
     * @return Объявление поля — найденное с подъёмом по иерархии наследования
     */
    public Field declaration() {
        return new Declared(this.target.getClass()).field(this.name);
    }

    /**
     * Значение поля.
     * @return Значение поля
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object value() {
        final Field field = this.declaration();
        field.setAccessible(true);
        try {
            return field.get(this.target);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Не удалось прочитать поле %s", this.name), denied
            );
        }
    }

    /**
     * Записывает значение в поле в обход сеттера и модификатора доступа.
     * @param value Значение
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public void assign(final Object value) {
        final Field field = this.declaration();
        field.setAccessible(true);
        try {
            field.set(this.target, value);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Не удалось записать поле %s", this.name), denied
            );
        }
    }
}

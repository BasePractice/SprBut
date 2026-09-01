/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 16: «Используется в таких фреймворках как Spring, Hibernate, JavaEE».
 *
 * <p>Соглашение JavaBeans поддержано прямо в JDK: пакет {@code java.beans} сам
 * находит пары getter/setter и отдаёт их как {@link PropertyDescriptor}.
 * Ровно этим пользуется {@code org.springframework.beans.BeanWrapper}
 * при биндинге данных формы.</p>
 *
 * <p>Служебное свойство {@code class} — от {@code Object.getClass()} — есть
 * у любого объекта и в отчётах только мешает, поэтому отфильтровано.</p>
 *
 * @since 1.0
 */
public final class Introspected {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Introspected(final Class<?> type) {
        this.type = type;
    }

    /**
     * Имена всех свойств бина.
     * @return Имена всех свойств бина
     */
    public List<String> names() {
        return this.descriptors().stream()
            .map(PropertyDescriptor::getName)
            .filter(name -> !"class".equals(name))
            .sorted()
            .toList();
    }

    /**
     * Свойства, у которых есть и getter, и setter, — по-настоящему управляемые контейнером.
     * @return Свойства, у которых есть и getter, и setter, — по-настоящему управляемые контейнером
     */
    public List<String> readWrite() {
        return this.descriptors().stream()
            .filter(property -> property.getReadMethod() != null)
            .filter(property -> property.getWriteMethod() != null)
            .map(PropertyDescriptor::getName)
            .sorted()
            .toList();
    }

    /**
     * Свойства только для чтения: {@code getFullName()} без сеттера.
     * @return Свойства только для чтения: {@code getFullName()} без сеттера
     */
    public List<String> readOnly() {
        return this.descriptors().stream()
            .filter(property -> property.getReadMethod() != null)
            .filter(property -> property.getWriteMethod() == null)
            .map(PropertyDescriptor::getName)
            .filter(name -> !"class".equals(name))
            .sorted()
            .toList();
    }

    /**
     * Описание одного свойства по имени.
     * @param property Имя свойства
     * @return Описание одного свойства по имени
     */
    public Optional<PropertyDescriptor> descriptor(final String property) {
        return this.descriptors().stream()
            .filter(candidate -> candidate.getName().equals(property))
            .findFirst();
    }

    /**
     * Все описания свойств, как их видит {@link Introspector}.
     * @return Все описания свойств, как их видит {@link Introspector}
     */
    public List<PropertyDescriptor> descriptors() {
        try {
            final BeanInfo info = Introspector.getBeanInfo(this.type);
            return Arrays.asList(info.getPropertyDescriptors());
        } catch (final IntrospectionException failure) {
            throw new IllegalStateException(
                "Introspector не смог разобрать " + this.type.getName(), failure
            );
        }
    }
}

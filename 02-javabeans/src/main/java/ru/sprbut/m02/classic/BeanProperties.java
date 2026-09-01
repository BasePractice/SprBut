/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 14: «Свойства объекта должны быть доступны через setter и getter».
 *
 * <p>Свойства класса, вычисленные по именам методов — так же, как это делает
 * любой фреймворк, которому не разрешили заглядывать в поля.</p>
 *
 * <p>Главное следствие соглашения видно именно здесь: свойство определяется
 * <b>методами, а не полями</b>. У класса может не быть поля {@code fullName},
 * но пока есть {@code getFullName()}, свойство существует.</p>
 *
 * @since 1.0
 */
public final class BeanProperties {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public BeanProperties(final Class<?> type) {
        this.type = type;
    }

    /**
     * Свойства, доступные на чтение: {@code getXxx()} и {@code isXxx()}.
     * @return Свойства, доступные на чтение: {@code getXxx()} и {@code isXxx()}
     */
    public List<String> readable() {
        final List<String> names = new ArrayList<>(0);
        for (final Method method : this.type.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.getParameterCount() != 0) {
                continue;
            }
            final String name = method.getName();
            if (name.startsWith("get") && name.length() > 3
                && method.getReturnType() != void.class) {
                names.add(new PropertyKey(name.substring(3)).decapitalized());
            } else if (name.startsWith("is") && name.length() > 2
                && BeanProperties.boolish(method)) {
                names.add(new PropertyKey(name.substring(2)).decapitalized());
            }
        }
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    /**
     * Свойства, доступные на запись: {@code setXxx(T)} с ровно одним параметром.
     * @return Свойства, доступные на запись: {@code setXxx(T)} с ровно одним параметром
     */
    public List<String> writable() {
        final List<String> names = new ArrayList<>(0);
        for (final Method method : this.type.getMethods()) {
            if (method.getParameterCount() == 1 && method.getName().startsWith("set")
                && method.getName().length() > 3) {
                names.add(new PropertyKey(method.getName().substring(3)).decapitalized());
            }
        }
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    /**
     * Метод чтения свойства, если он существует.
     * @param property Имя свойства
     * @return Метод чтения свойства, если он существует
     */
    public Method reader(final String property) {
        final String suffix = new PropertyKey(property).capitalized();
        Method found = null;
        for (final String prefix : new String[]{"get", "is"}) {
            if (found == null) {
                try {
                    found = this.type.getMethod(String.format("%s%s", prefix, suffix));
                } catch (final NoSuchMethodException absent) {
                    assert absent != null;
                }
            }
        }
        return found;
    }

    private static boolean boolish(final Method method) {
        return method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
    }
}

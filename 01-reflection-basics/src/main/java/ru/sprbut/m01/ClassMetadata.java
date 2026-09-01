/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Слайды 3–5: «Механизм работы с метаданными объектов в runtime»,
 * «Позволяет узнать имя класса объекта».
 *
 * <p>Метаданные одного класса: имена, иерархия, интерфейсы. Объект строится
 * от {@code Class} или сразу от экземпляра — это и есть два из трёх способов
 * получить {@code Class}, о которых говорит слайд; третий, {@code Class.forName},
 * живёт в {@link ClassByName}.</p>
 *
 * @since 1.0
 */
public final class ClassMetadata {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     */
    public ClassMetadata(final Object target) {
        this(target.getClass());
    }

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public ClassMetadata(final Class<?> type) {
        this.type = type;
    }

    /**
     * Полное имя класса вместе с пакетом: {@code ru.sprbut.m01.model.Account}.
     * @return Полное имя класса вместе с пакетом: {@code ru.sprbut.m01.model.Account}
     */
    public String fullName() {
        return this.type.getName();
    }

    /**
     * Короткое имя без пакета: {@code Account}.
     * @return Короткое имя без пакета: {@code Account}
     */
    public String simpleName() {
        return this.type.getSimpleName();
    }

    /**
     * Имя пакета, в котором объявлен класс.
     * @return Имя пакета, в котором объявлен класс
     */
    public String packageName() {
        return this.type.getPackageName();
    }

    /**
     * Цепочка наследования до {@link Object} включительно.
     * Именно так фреймворки ищут аннотации и поля в родителях.
     * @return Цепочка наследования до {@link Object} включительно
     */
    public List<String> hierarchy() {
        final List<String> names = new ArrayList<>(0);
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            names.add(current.getSimpleName());
        }
        return List.copyOf(names);
    }

    /**
     * Интерфейсы, которые класс реализует напрямую.
     * @return Интерфейсы, которые класс реализует напрямую
     */
    public List<String> interfaces() {
        return Arrays.stream(this.type.getInterfaces())
            .map(Class::getSimpleName)
            .toList();
    }

    /**
     * Признаки, по которым фреймворки решают, можно ли создать экземпляр типа.
     * @return Признаки, по которым фреймворки решают, можно ли создать экземпляр типа
     */
    public boolean instantiable() {
        return !this.type.isInterface()
            && !Modifier.isAbstract(this.type.getModifiers())
            && !this.type.isPrimitive()
            && !this.type.isEnum()
            && !this.type.isArray();
    }
}

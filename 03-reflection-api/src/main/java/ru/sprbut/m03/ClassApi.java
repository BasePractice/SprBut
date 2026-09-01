/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Слайды 26–27 (СХЕМА 1): {@link Class} — центр карты Reflection API.
 *
 * <p>Всё остальное — {@code Field}, {@code Method}, {@code Constructor} — добывается
 * из него. Сам {@code Class} при этом отвечает и на вопросы о природе типа:
 * массив ли это, enum, record, вложенный класс.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ClassApi {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public ClassApi(final Class<?> type) {
        this.type = type;
    }

    /**
     * Имена объявленных полей в алфавитном порядке.
     * @return Имена объявленных полей в алфавитном порядке
     */
    public List<String> fields() {
        return Arrays.stream(this.type.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .map(Field::getName)
            .sorted()
            .toList();
    }

    /**
     * Имена объявленных методов без повторов от перегрузок.
     * @return Имена объявленных методов без повторов от перегрузок
     */
    public List<String> methods() {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(method -> !method.isSynthetic())
            .map(Method::getName)
            .sorted()
            .distinct()
            .toList();
    }

    /**
     * Сколько всего конструкторов объявлено, включая непубличные.
     * @return Сколько всего конструкторов объявлено, включая непубличные
     */
    public int constructorCount() {
        return this.type.getDeclaredConstructors().length;
    }

    /**
     * Тип элемента массива: {@code String} для {@code String[]}, иначе {@code null}.
     * @return Тип элемента массива: {@code String} для {@code String[]}, иначе {@code null}
     */
    public Class<?> componentType() {
        return this.type.getComponentType();
    }

    /**
     * Класс, внутри которого объявлен вложенный тип.
     * @return Класс, внутри которого объявлен вложенный тип
     */
    public Class<?> enclosing() {
        return this.type.getEnclosingClass();
    }

    /**
     * Иерархия наследования снизу вверх, до {@code Object} включительно.
     * @return Иерархия наследования снизу вверх, до {@code Object} включительно
     */
    public List<String> superChain() {
        final List<String> chain = new ArrayList<>(0);
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            chain.add(current.getSimpleName());
        }
        return List.copyOf(chain);
    }

    /**
     * Все интерфейсы, включая унаследованные, — по ним фреймворки решают,
     * подходит ли бин под тип зависимости.
     * @return Все интерфейсы, включая унаследованные
     */
    public List<String> allInterfaces() {
        final Set<String> collected = new TreeSet<>();
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            this.collect(current, collected);
        }
        return List.copyOf(collected);
    }

    /**
     * Может ли переменная этого типа хранить значение другого.
     *
     * <p>{@code isAssignableFrom} читается наоборот, чем кажется:
     * {@code Number.class.isAssignableFrom(Integer.class)} — истина.</p>
     *
     * @param actual Проверяемый тип
     * @return Может ли переменная этого типа хранить значение другого
     */
    public boolean canHold(final Class<?> actual) {
        return this.type.isAssignableFrom(actual);
    }

    /**
     * Компоненты record — отдельная сущность API, появившаяся в Java 16.
     * @return Компоненты record
     */
    public List<String> recordComponents() {
        final List<String> found;
        if (this.type.isRecord()) {
            found = Arrays.stream(this.type.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
        } else {
            found = List.of();
        }
        return found;
    }

    /**
     * Константы enum в порядке объявления.
     * @return Константы enum в порядке объявления
     */
    public List<String> enumConstants() {
        final Object[] constants = this.type.getEnumConstants();
        final List<String> found;
        if (constants == null) {
            found = List.of();
        } else {
            found = Arrays.stream(constants).map(String::valueOf).toList();
        }
        return found;
    }

    /**
     * Новый массив этого типа элементов.
     *
     * <p>Массив создаётся не конструктором, а фабрикой {@link Array} — отдельная
     * ветка API, которую легко упустить.</p>
     *
     * @param length Длина
     * @return Новый массив этого типа элементов
     */
    public Object array(final int length) {
        return Array.newInstance(this.type, length);
    }

    private void collect(final Class<?> from, final Set<String> sink) {
        for (final Class<?> each : from.getInterfaces()) {
            if (sink.add(each.getSimpleName())) {
                this.collect(each, sink);
            }
        }
    }
}

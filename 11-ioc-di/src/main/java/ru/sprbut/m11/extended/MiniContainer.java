/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.extended;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>Расширенный пример модуля 11.</b>
 *
 * <p>Работающий IoC-контейнер на ~150 строк. Он делает ровно то, что перечислено
 * на слайдах 84–87, и ничего сверх того:
 * <ul>
 * <li><b>инверсия управления</b> — объекты создаёт контейнер, а не они себя сами;</li>
 * <li><b>фабрика</b> — знает, как создать экземпляр (рефлексия из модуля 03);</li>
 * <li><b>внедрение зависимостей</b> — аргументы конструктора подбираются по типу;</li>
 * <li><b>жизненный цикл</b> — синглтоны создаются один раз и кэшируются.</li>
 * </ul>
 * Отдельная ценность — <b>ошибки</b>: контейнер честно воспроизводит три
 * ситуации, из-за которых чаще всего не стартует настоящий Spring
 * (модуль 21): бин не найден, бинов слишком много, циклическая зависимость.</p>
 *
 * <p>Порядок создания нигде не задаётся: он вычисляется из графа зависимостей.
 * Это и есть то, ради чего ручную фабрику {@link ru.sprbut.m11.step2.ObjectFactory}
 * меняют на контейнер.</p>
 *
 * @since 1.0
 */
public class MiniContainer {

    /**
     * Определения: имя → класс.
     */
    private final Map<String, Class<?>> definitions = new LinkedHashMap<>();

    /**
     * Готовые синглтоны: имя → экземпляр.
     */
    private final Map<String, Object> singletons = new LinkedHashMap<>();

    /**
     * Классы, которые сейчас находятся в процессе создания — детектор циклов.
     */
    private final Set<Class<?>> inCreation = new LinkedHashSet<>();

    /**
     * Порядок фактического создания бинов — виден в тестах.
     */
    private final List<String> creationOrder = new ArrayList<>();

    /**
     * Основной конструктор.
     * @param componentClasses Значение {@code componentClasses}
     */
    public MiniContainer(final Class<?>... componentClasses) {
        for (final Class<?> type : componentClasses) {
            this.register(type);
        }
    }

    /**
     * Регистрация определения. Экземпляр пока не создаётся — только описание.
     * @param type Тип
     */
    public final void register(final Class<?> type) {
        final MiniComponent annotation = type.getAnnotation(MiniComponent.class);
        if (
            annotation == null
        ) {
            throw new IllegalArgumentException(
                type.getSimpleName()
                    + " не помечен @MiniComponent — контейнер такими классами не управляет"
            );
        }
        final String name = annotation.value().isBlank() ? defaultName(type) : annotation.value();
        final Class<?> previous = this.definitions.put(name, type);
        if (
            previous != null
        ) {
            throw new IllegalStateException(
                String.format("Имя бина '%s' уже занято классом %s", name, previous.getSimpleName())
            );
        }
    }

    /**
     * Создаёт все зарегистрированные бины сразу — как делает Spring для синглтонов.
     * @return Создаёт все зарегистрированные бины сразу — как делает Spring для синглтонов
     */
    public MiniContainer refresh() {
        for (final String name : List.copyOf(this.definitions.keySet())) {
            this.getBean(name);
        }
        return this;
    }

    /**
     * Достать бин по имени.
     * @param name Имя
     * @return Достать бин по имени
     */
    public Object getBean(final String name) {
        final Class<?> type = this.definitions.get(name);
        if (
            type == null
        ) {
            throw new NoSuchBeanException(
                "Нет бина с именем '" + name + "'; известны: " + this.definitions.keySet()
            );
        }
        return this.instantiate(name, type);
    }

    /**
     * Достать бин по типу. Подходит и точное совпадение, и реализация интерфейса —
     * ровно как в Spring.
     * @param requiredType Тип
     * @return Достать бин по типу. Подходит и точное совпадение, и реализация интерфейса — ровно как в Spring
     */
    public <T> T getBean(
        final Class<T> requiredType
    ) {
        final List<String> candidates = this.definitions.entrySet().stream()
                .filter(
                    e -> requiredType.isAssignableFrom(e.getValue())
                )
                .map(Map.Entry::getKey)
                .toList();
        if (candidates.isEmpty()) {
            throw new NoSuchBeanException("Нет бина типа " + requiredType.getSimpleName());
        }
        if (
            candidates.size() > 1
        ) {
            throw new NoUniqueBeanException(
                "Бинов типа "
                    + requiredType.getSimpleName()
                    + " несколько: "
                    + candidates
                    + ". Нужен квалификатор или @Primary"
            );
        }
        return requiredType.cast(this.getBean(candidates.get(0)));
    }

    /**
     * Объект.
     * @return Объект
     */
    public Set<String> beanNames() {
        return Set.copyOf(this.definitions.keySet());
    }

    /**
     * Порядок.
     * @return Порядок
     */
    public List<String> creationOrder() {
        return List.copyOf(this.creationOrder);
    }

    /**
     * Значение: момент создания.
     * @param name Имя
     * @return Значение: момент создания
     */
    public boolean isCreated(final String name) {
        return this.singletons.containsKey(name);
    }

    // --- Создание -----------------------------------------------------------

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private Object instantiate(final String name, final Class<?> type) {
        final Object existing = this.singletons.get(name);
        if (existing != null) {
            return existing;
        }
        if (
            !this.inCreation.add(type)
        ) {
            throw new CircularDependencyException(
                "Циклическая зависимость: "
                    + this.inCreation.stream().map(Class::getSimpleName).toList()
                    + " → "
                    + type.getSimpleName()
            );
        }
        try {
            final Constructor<?> constructor = selectConstructor(type);
            // Рекурсия: сначала создаём зависимости, потом сам бин.
            // Порядок создания вычисляется отсюда сам собой.
            final Object[] args = Arrays.stream(
                constructor.getParameterTypes()
            )
                    .map(
                        this::getBean
                    )
                    .toArray();
            constructor.setAccessible(true);
            final Object bean = constructor.newInstance(args);
            this.singletons.put(name, bean);
            this.creationOrder.add(name);
            return bean;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Не удалось создать " + type.getSimpleName(), e);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re
                    ? re
                    : new IllegalStateException("Конструктор " + type.getSimpleName() + " бросил исключение", cause);
        } finally {
            this.inCreation.remove(type);
        }
    }

    /**
     * Правило выбора конструктора — то же, что в Spring: если конструктор один,
     * он и используется, никаких аннотаций не нужно.
     * @param type Тип
     * @return Правило выбора конструктора — то же, что в Spring: если конструктор один, он и используется, никаких аннотаций не нужно
     */
    static Constructor<?> selectConstructor(final Class<?> type) {
        final Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        return Arrays.stream(
            constructors
        )
                .filter(
                    c -> c.getParameterCount() == 0
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("У " + type.getSimpleName()
                        + " несколько конструкторов и нет конструктора без параметров — "
                        + "контейнер не знает, какой выбрать"));
    }

    static String defaultName(final Class<?> type) {
        final String simpleName = type.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    // --- Ошибки, повторяющие поведение Spring -------------------------------

    /**
     * Аналог {@code NoSuchBeanDefinitionException}.
     * @since 1.0
     */
    public static class NoSuchBeanException extends RuntimeException {

        /**
         * Основной конструктор.
         * @param message Сообщение
         */
        public NoSuchBeanException(final String message) {
            super(message);
        }
    }

    /**
     * Аналог {@code NoUniqueBeanDefinitionException}.
     * @since 1.0
     */
    public static class NoUniqueBeanException extends RuntimeException {

        /**
         * Основной конструктор.
         * @param message Сообщение
         */
        public NoUniqueBeanException(final String message) {
            super(message);
        }
    }

    /**
     * Аналог {@code BeanCurrentlyInCreationException}.
     * @since 1.0
     */
    public static class CircularDependencyException extends RuntimeException {

        /**
         * Основной конструктор.
         * @param message Сообщение
         */
        public CircularDependencyException(final String message) {
            super(message);
        }
    }
}

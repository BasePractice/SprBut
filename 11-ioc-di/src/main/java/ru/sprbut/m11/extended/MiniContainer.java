/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// исключения контейнера живут рядом с ним: они и есть часть его контракта
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle MemberNameCheck disable
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
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
public class MiniContainer {

    /**
     * Определения: имя ведёт к класс.
     */
    private final Map<String, Class<?>> definitions = new LinkedHashMap<>();

    /**
     * Готовые синглтоны: имя ведёт к экземпляр.
     */
    private final Map<String, Object> singletons = new LinkedHashMap<>();

    /**
     * Классы, которые сейчас находятся в процессе создания — детектор циклов.
     */
    private final Set<Class<?>> inCreation = new LinkedHashSet<>();

    /**
     * Порядок фактического создания бинов — виден в тестах.
     */
    private final List<String> creationOrder = new ArrayList<>(0);

    /**
     * Основной конструктор.
     * Регистрация определений идёт прямо здесь: контейнер и создаётся
     * для того, чтобы знать про эти классы.
     * @param componentClasses Классы компонентов
     * @checkstyle ConstructorsCodeFreeCheck (6 lines)
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
        if (annotation == null) {
            throw new IllegalArgumentException(
                String.format(
                    "%s не помечен @MiniComponent — контейнер такими классами не управляет",
                    type.getSimpleName()
                )
            );
        }
        final String name;
        if (annotation.value().isBlank()) {
            name = MiniContainer.defaultName(type);
        } else {
            name = annotation.value();
        }
        final Class<?> previous = this.definitions.put(name, type);
        if (previous != null) {
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
        if (type == null) {
            throw new MiniContainer.NoSuchBeanException(
                String.format(
                    "Нет бина с именем '%s'; известны: %s", name, this.definitions.keySet()
                )
            );
        }
        return this.instantiate(name, type);
    }

    /**
     * Достать бин по типу. Подходит и точное совпадение, и реализация интерфейса —
     * ровно как в Spring.
     * @param requiredType Тип
     * @param <T> Тип бина
     * @return Бин запрошенного типа
     */
    public <T> T getBean(final Class<T> requiredType) {
        final List<String> candidates = this.definitions.entrySet().stream()
            .filter(entry -> requiredType.isAssignableFrom(entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
        if (candidates.isEmpty()) {
            throw new MiniContainer.NoSuchBeanException(
                String.format("Нет бина типа %s", requiredType.getSimpleName())
            );
        }
        if (candidates.size() > 1) {
            throw new MiniContainer.NoUniqueBeanException(
                String.format(
                    "Бинов типа %s несколько: %s. Нужен квалификатор или @Primary",
                    requiredType.getSimpleName(), candidates
                )
            );
        }
        return requiredType.cast(this.getBean(candidates.get(0)));
    }

    /**
     * Имена всех известных бинов.
     * @return Имена всех известных бинов
     */
    public Set<String> beanNames() {
        return Set.copyOf(this.definitions.keySet());
    }

    /**
     * Порядок создания бинов.
     * @return Порядок создания бинов
     */
    public List<String> creationOrder() {
        return List.copyOf(this.creationOrder);
    }

    /**
     * Создан ли бин с этим именем.
     * @param name Имя
     * @return Признак того, что бин уже создан
     */
    public boolean isCreated(final String name) {
        return this.singletons.containsKey(name);
    }

    /**
     * Правило выбора конструктора — то же, что в Spring: если конструктор один,
     * он и используется, никаких аннотаций не нужно.
     * @param type Тип
     * @return Конструктор, которым контейнер создаст бин
     */
    static Constructor<?> selectConstructor(final Class<?> type) {
        final Constructor<?>[] constructors = type.getDeclaredConstructors();
        final Constructor<?> chosen;
        if (constructors.length == 1) {
            chosen = constructors[0];
        } else {
            chosen = Arrays.stream(constructors)
                .filter(candidate -> candidate.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(
                    () -> new IllegalStateException(
                        String.format(
                            "У %s несколько конструкторов и нет конструктора без параметров",
                            type.getSimpleName()
                        )
                    )
                );
        }
        return chosen;
    }

    static String defaultName(final Class<?> type) {
        final String simple = type.getSimpleName();
        return String.format(
            "%s%s", Character.toLowerCase(simple.charAt(0)), simple.substring(1)
        );
    }

    // рекурсия: сначала создаются зависимости, потом сам бин,
    // поэтому порядок создания вычисляется сам собой
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private Object created(final String name, final Class<?> type) {
        if (!this.inCreation.add(type)) {
            throw new MiniContainer.CircularDependencyException(
                String.format(
                    "Циклическая зависимость: %s ведёт к %s",
                    this.inCreation.stream().map(Class::getSimpleName).toList(),
                    type.getSimpleName()
                )
            );
        }
        try {
            final Constructor<?> constructor = MiniContainer.selectConstructor(type);
            final Object[] args = Arrays.stream(constructor.getParameterTypes())
                .map(this::getBean)
                .toArray();
            constructor.setAccessible(true);
            final Object bean = constructor.newInstance(args);
            this.singletons.put(name, bean);
            this.creationOrder.add(name);
            return bean;
        } catch (final InstantiationException | IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Не удалось создать %s", type.getSimpleName()), denied
            );
        } catch (final InvocationTargetException wrapped) {
            throw new IllegalStateException(
                String.format("Конструктор %s бросил исключение", type.getSimpleName()), wrapped
            );
        } finally {
            this.inCreation.remove(type);
        }
    }

    // --- Создание -----------------------------------------------------------

    private Object instantiate(final String name, final Class<?> type) {
        final Object existing = this.singletons.get(name);
        final Object bean;
        if (existing == null) {
            bean = this.created(name, type);
        } else {
            bean = existing;
        }
        return bean;
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

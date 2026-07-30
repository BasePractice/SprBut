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
 * <p>
 * Работающий IoC-контейнер на ~150 строк. Он делает ровно то, что перечислено
 * на слайдах 84–87, и ничего сверх того:
 * <ul>
 *   <li><b>инверсия управления</b> — объекты создаёт контейнер, а не они себя сами;</li>
 *   <li><b>фабрика</b> — знает, как создать экземпляр (рефлексия из модуля 03);</li>
 *   <li><b>внедрение зависимостей</b> — аргументы конструктора подбираются по типу;</li>
 *   <li><b>жизненный цикл</b> — синглтоны создаются один раз и кэшируются.</li>
 * </ul>
 * Отдельная ценность — <b>ошибки</b>: контейнер честно воспроизводит три
 * ситуации, из-за которых чаще всего не стартует настоящий Spring
 * (модуль 21): бин не найден, бинов слишком много, циклическая зависимость.
 * <p>
 * Порядок создания нигде не задаётся: он вычисляется из графа зависимостей.
 * Это и есть то, ради чего ручную фабрику {@link ru.sprbut.m11.step2.ObjectFactory}
 * меняют на контейнер.
 */
public class MiniContainer {

    /** Определения: имя → класс. */
    private final Map<String, Class<?>> definitions = new LinkedHashMap<>();

    /** Готовые синглтоны: имя → экземпляр. */
    private final Map<String, Object> singletons = new LinkedHashMap<>();

    /** Классы, которые сейчас находятся в процессе создания — детектор циклов. */
    private final Set<Class<?>> inCreation = new LinkedHashSet<>();

    /** Порядок фактического создания бинов — виден в тестах. */
    private final List<String> creationOrder = new ArrayList<>();

    public MiniContainer(Class<?>... componentClasses) {
        for (Class<?> type : componentClasses) {
            register(type);
        }
    }

    /** Регистрация определения. Экземпляр пока не создаётся — только описание. */
    public final void register(Class<?> type) {
        MiniComponent annotation = type.getAnnotation(MiniComponent.class);
        if (annotation == null) {
            throw new IllegalArgumentException(type.getSimpleName()
                    + " не помечен @MiniComponent — контейнер такими классами не управляет");
        }
        String name = annotation.value().isBlank() ? defaultName(type) : annotation.value();
        Class<?> previous = definitions.put(name, type);
        if (previous != null) {
            throw new IllegalStateException("Имя бина '" + name + "' уже занято классом "
                    + previous.getSimpleName());
        }
    }

    /** Создаёт все зарегистрированные бины сразу — как делает Spring для синглтонов. */
    public MiniContainer refresh() {
        for (String name : List.copyOf(definitions.keySet())) {
            getBean(name);
        }
        return this;
    }

    /** Достать бин по имени. */
    public Object getBean(String name) {
        Class<?> type = definitions.get(name);
        if (type == null) {
            throw new NoSuchBeanException("Нет бина с именем '" + name
                    + "'; известны: " + definitions.keySet());
        }
        return instantiate(name, type);
    }

    /**
     * Достать бин по типу. Подходит и точное совпадение, и реализация интерфейса —
     * ровно как в Spring.
     */
    public <T> T getBean(Class<T> requiredType) {
        List<String> candidates = definitions.entrySet().stream()
                .filter(e -> requiredType.isAssignableFrom(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        if (candidates.isEmpty()) {
            throw new NoSuchBeanException("Нет бина типа " + requiredType.getSimpleName());
        }
        if (candidates.size() > 1) {
            throw new NoUniqueBeanException("Бинов типа " + requiredType.getSimpleName()
                    + " несколько: " + candidates + ". Нужен квалификатор или @Primary");
        }
        return requiredType.cast(getBean(candidates.get(0)));
    }

    public Set<String> beanNames() {
        return Set.copyOf(definitions.keySet());
    }

    public List<String> creationOrder() {
        return List.copyOf(creationOrder);
    }

    public boolean isCreated(String name) {
        return singletons.containsKey(name);
    }

    // --- Создание -----------------------------------------------------------

    private Object instantiate(String name, Class<?> type) {
        Object existing = singletons.get(name);
        if (existing != null) {
            return existing;
        }
        if (!inCreation.add(type)) {
            throw new CircularDependencyException("Циклическая зависимость: "
                    + inCreation.stream().map(Class::getSimpleName).toList()
                    + " → " + type.getSimpleName());
        }
        try {
            Constructor<?> constructor = selectConstructor(type);
            // Рекурсия: сначала создаём зависимости, потом сам бин.
            // Порядок создания вычисляется отсюда сам собой.
            Object[] args = Arrays.stream(constructor.getParameterTypes())
                    .map(this::getBean)
                    .toArray();
            constructor.setAccessible(true);
            Object bean = constructor.newInstance(args);
            singletons.put(name, bean);
            creationOrder.add(name);
            return bean;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Не удалось создать " + type.getSimpleName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re
                    ? re
                    : new IllegalStateException("Конструктор " + type.getSimpleName() + " бросил исключение", cause);
        } finally {
            inCreation.remove(type);
        }
    }

    /**
     * Правило выбора конструктора — то же, что в Spring: если конструктор один,
     * он и используется, никаких аннотаций не нужно.
     */
    static Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        return Arrays.stream(constructors)
                .filter(c -> c.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("У " + type.getSimpleName()
                        + " несколько конструкторов и нет конструктора без параметров — "
                        + "контейнер не знает, какой выбрать"));
    }

    static String defaultName(Class<?> type) {
        String simpleName = type.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    // --- Ошибки, повторяющие поведение Spring -------------------------------

    /** Аналог {@code NoSuchBeanDefinitionException}. */
    public static class NoSuchBeanException extends RuntimeException {
        public NoSuchBeanException(String message) {
            super(message);
        }
    }

    /** Аналог {@code NoUniqueBeanDefinitionException}. */
    public static class NoUniqueBeanException extends RuntimeException {
        public NoUniqueBeanException(String message) {
            super(message);
        }
    }

    /** Аналог {@code BeanCurrentlyInCreationException}. */
    public static class CircularDependencyException extends RuntimeException {
        public CircularDependencyException(String message) {
            super(message);
        }
    }
}

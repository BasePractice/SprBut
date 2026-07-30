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
 * <p>
 * Всё остальное — {@code Field}, {@code Method}, {@code Constructor} — добывается
 * из него. Сам {@code Class} при этом отвечает и на вопросы о природе типа:
 * массив ли это, enum, record, вложенный класс.
 */
public final class ClassApi {

    private final Class<?> type;

    public ClassApi(Class<?> type) {
        this.type = type;
    }

    /**
     * Имена объявленных полей в алфавитном порядке.
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
     */
    public int constructorCount() {
        return this.type.getDeclaredConstructors().length;
    }

    /**
     * Тип элемента массива: {@code String} для {@code String[]}, иначе {@code null}.
     */
    public Class<?> componentType() {
        return this.type.getComponentType();
    }

    /**
     * Класс, внутри которого объявлен вложенный тип.
     */
    public Class<?> enclosing() {
        return this.type.getEnclosingClass();
    }

    /**
     * Иерархия наследования снизу вверх, до {@code Object} включительно.
     */
    public List<String> superChain() {
        List<String> chain = new ArrayList<>();
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            chain.add(current.getSimpleName());
        }
        return List.copyOf(chain);
    }

    /**
     * Все интерфейсы, включая унаследованные, — по ним фреймворки решают,
     * подходит ли бин под тип зависимости.
     */
    public List<String> allInterfaces() {
        Set<String> collected = new TreeSet<>();
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            collect(current, collected);
        }
        return List.copyOf(collected);
    }

    /**
     * Может ли переменная этого типа хранить значение другого.
     * <p>
     * {@code isAssignableFrom} читается наоборот, чем кажется:
     * {@code Number.class.isAssignableFrom(Integer.class)} — истина.
     */
    public boolean canHold(Class<?> actual) {
        return this.type.isAssignableFrom(actual);
    }

    /**
     * Компоненты record — отдельная сущность API, появившаяся в Java 16.
     */
    public List<String> recordComponents() {
        if (!this.type.isRecord()) {
            return List.of();
        }
        return Arrays.stream(this.type.getRecordComponents())
            .map(RecordComponent::getName)
            .toList();
    }

    /**
     * Константы enum в порядке объявления.
     */
    public List<String> enumConstants() {
        Object[] constants = this.type.getEnumConstants();
        if (constants == null) {
            return List.of();
        }
        return Arrays.stream(constants).map(String::valueOf).toList();
    }

    /**
     * Новый массив этого типа элементов.
     * <p>
     * Массив создаётся не конструктором, а фабрикой {@link Array} — отдельная
     * ветка API, которую легко упустить.
     */
    public Object array(int length) {
        return Array.newInstance(this.type, length);
    }

    private void collect(Class<?> from, Set<String> sink) {
        for (Class<?> each : from.getInterfaces()) {
            if (sink.add(each.getSimpleName())) {
                collect(each, sink);
            }
        }
    }
}

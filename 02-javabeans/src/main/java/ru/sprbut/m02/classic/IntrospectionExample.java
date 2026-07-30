package ru.sprbut.m02.classic;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Слайд 16: JavaBeans — соглашение, на котором стоят Spring, Hibernate, JavaEE.
 * <p>
 * В JDK есть штатный инструмент для работы с этим соглашением — пакет
 * {@code java.beans}. {@link Introspector} сам находит пары getter/setter и
 * отдаёт их как {@link PropertyDescriptor}. Ровно этим механизмом пользуется
 * {@code org.springframework.beans.BeanWrapper} при биндинге данных формы.
 */
public final class IntrospectionExample {

    private IntrospectionExample() {
    }

    /**
     * Имена всех свойств бина. Служебное свойство {@code class} (от
     * {@code Object.getClass()}) отфильтровано — оно есть у любого объекта.
     */
    public static List<String> propertyNames(Class<?> type) {
        return descriptors(type).stream()
                .map(PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .sorted()
                .toList();
    }

    /**
     * Свойства, у которых есть и getter, и setter — то есть по-настоящему
     * читаемые и записываемые контейнером.
     */
    public static List<String> readWriteProperties(Class<?> type) {
        return descriptors(type).stream()
                .filter(pd -> pd.getReadMethod() != null && pd.getWriteMethod() != null)
                .map(PropertyDescriptor::getName)
                .sorted()
                .toList();
    }

    /**
     * Свойства только для чтения: {@code getFullName()} без сеттера.
     * Показывает, что свойство определяется методами, а не полями.
     */
    public static List<String> readOnlyProperties(Class<?> type) {
        return descriptors(type).stream()
                .filter(pd -> pd.getReadMethod() != null && pd.getWriteMethod() == null)
                .map(PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .sorted()
                .toList();
    }

    public static Optional<PropertyDescriptor> descriptor(Class<?> type, String property) {
        return descriptors(type).stream()
                .filter(pd -> pd.getName().equals(property))
                .findFirst();
    }

    /**
     * Чтение свойства по имени — без знания класса на этапе компиляции.
     */
    public static Object read(Object bean, String property) {
        PropertyDescriptor pd = descriptor(bean.getClass(), property)
                .orElseThrow(() -> new IllegalArgumentException("Нет свойства '" + property + "'"));
        Method reader = pd.getReadMethod();
        if (reader == null) {
            throw new IllegalArgumentException("Свойство '" + property + "' недоступно на чтение");
        }
        return invoke(reader, bean);
    }

    /**
     * Запись свойства по имени. Так контейнер заполняет бин из properties/yaml.
     */
    public static void write(Object bean, String property, Object value) {
        PropertyDescriptor pd = descriptor(bean.getClass(), property)
                .orElseThrow(() -> new IllegalArgumentException("Нет свойства '" + property + "'"));
        Method writer = pd.getWriteMethod();
        if (writer == null) {
            throw new IllegalArgumentException("Свойство '" + property + "' доступно только на чтение");
        }
        invoke(writer, bean, value);
    }

    /**
     * Весь бин как Map «свойство → значение». Порядок сохраняется отсортированным,
     * чтобы результат был предсказуемым.
     */
    public static Map<String, Object> toMap(Object bean) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String name : propertyNames(bean.getClass())) {
            PropertyDescriptor pd = descriptor(bean.getClass(), name).orElseThrow();
            if (pd.getReadMethod() != null) {
                result.put(name, invoke(pd.getReadMethod(), bean));
            }
        }
        return result;
    }

    public static List<PropertyDescriptor> descriptors(Class<?> type) {
        try {
            BeanInfo info = Introspector.getBeanInfo(type);
            return Arrays.asList(info.getPropertyDescriptors());
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Introspector не смог разобрать " + type.getName(), e);
        }
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к " + method.getName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re ? re : new IllegalStateException(cause);
        }
    }
}

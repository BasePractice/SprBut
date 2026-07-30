package ru.sprbut.m02.classic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 14: «Свойства объекта должны быть доступны через setter и getter».
 * <p>
 * Свойства класса, вычисленные по именам методов — так же, как это делает
 * любой фреймворк, которому не разрешили заглядывать в поля.
 * <p>
 * Главное следствие соглашения видно именно здесь: свойство определяется
 * <b>методами, а не полями</b>. У класса может не быть поля {@code fullName},
 * но пока есть {@code getFullName()}, свойство существует.
 */
public final class BeanProperties {

    private final Class<?> type;

    public BeanProperties(Class<?> type) {
        this.type = type;
    }

    /**
     * Свойства, доступные на чтение: {@code getXxx()} и {@code isXxx()}.
     */
    public List<String> readable() {
        List<String> names = new ArrayList<>();
        for (Method method : this.type.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3 && method.getReturnType() != void.class) {
                names.add(new PropertyKey(name.substring(3)).decapitalized());
            } else if (name.startsWith("is") && name.length() > 2 && boolish(method)) {
                names.add(new PropertyKey(name.substring(2)).decapitalized());
            }
        }
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    /**
     * Свойства, доступные на запись: {@code setXxx(T)} с ровно одним параметром.
     */
    public List<String> writable() {
        List<String> names = new ArrayList<>();
        for (Method method : this.type.getMethods()) {
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
     */
    public Method reader(String property) {
        String suffix = new PropertyKey(property).capitalized();
        for (String prefix : new String[]{"get", "is"}) {
            try {
                return this.type.getMethod(prefix + suffix);
            } catch (NoSuchMethodException absent) {
                continue;
            }
        }
        return null;
    }

    private boolean boolish(Method method) {
        return method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
    }
}

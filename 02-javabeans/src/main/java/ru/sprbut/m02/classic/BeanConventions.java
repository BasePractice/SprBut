package ru.sprbut.m02.classic;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайды 12–16: проверка того, что класс подчиняется соглашению JavaBeans.
 * <p>
 * Написана на рефлексии из модуля 01 — тем же способом, каким Spring и Hibernate
 * решают, умеют ли они работать с типом.
 */
public final class BeanConventions {

    private BeanConventions() {
    }

    /**
     * Результат проверки: подходит ли класс под соглашение и что именно нарушено.
     */
    public record Verdict(boolean valid, List<String> violations) {

        public Verdict {
            violations = List.copyOf(violations);
        }
    }

    /**
     * Строгая проверка по всем четырём пунктам слайда, включая {@link Serializable}.
     */
    public static Verdict validateStrict(Class<?> type) {
        List<String> violations = collectViolations(type);
        if (!Serializable.class.isAssignableFrom(type)) {
            violations.add("класс не реализует Serializable");
        }
        return new Verdict(violations.isEmpty(), violations);
    }

    /**
     * Проверка «как у Spring»: {@link Serializable} не требуется.
     * Слайд прямо это оговаривает — контейнеру нужен только конструктор и свойства.
     */
    public static Verdict validateSpringStyle(Class<?> type) {
        List<String> violations = collectViolations(type);
        return new Verdict(violations.isEmpty(), violations);
    }

    private static List<String> collectViolations(Class<?> type) {
        List<String> violations = new ArrayList<>();
        if (!hasPublicNoArgConstructor(type)) {
            violations.add("нет публичного конструктора без параметров");
        }
        for (String property : writableProperties(type)) {
            if (readerFor(type, property) == null) {
                violations.add("у свойства '" + property + "' есть setter, но нет getter");
            }
        }
        return violations;
    }

    /**
     * Первое требование соглашения: {@code public Xxx()}.
     * Без него контейнер не сможет создать объект дефолтным способом.
     */
    public static boolean hasPublicNoArgConstructor(Class<?> type) {
        for (Constructor<?> ctor : type.getConstructors()) {
            if (ctor.getParameterCount() == 0 && Modifier.isPublic(ctor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Свойства, доступные на чтение: методы вида {@code getXxx()} / {@code isXxx()}
     * без параметров и с непустым возвращаемым типом.
     */
    public static List<String> readableProperties(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (Method m : type.getMethods()) {
            if (m.getDeclaringClass() == Object.class || m.getParameterCount() != 0) {
                continue;
            }
            String name = m.getName();
            if (name.startsWith("get") && name.length() > 3 && m.getReturnType() != void.class) {
                result.add(decapitalize(name.substring(3)));
            } else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                result.add(decapitalize(name.substring(2)));
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    /**
     * Свойства, доступные на запись: {@code setXxx(T)} с ровно одним параметром.
     */
    public static List<String> writableProperties(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (Method m : type.getMethods()) {
            if (m.getParameterCount() != 1) {
                continue;
            }
            String name = m.getName();
            if (name.startsWith("set") && name.length() > 3) {
                result.add(decapitalize(name.substring(3)));
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    public static Method readerFor(Class<?> type, String property) {
        String suffix = capitalize(property);
        for (String prefix : new String[]{"get", "is"}) {
            try {
                return type.getMethod(prefix + suffix);
            } catch (NoSuchMethodException ignored) {
                // пробуем следующий префикс
            }
        }
        return null;
    }

    /**
     * Демонстрация мутабельности (слайд 18): бин можно создать пустым и заполнять
     * по частям, поэтому между вызовами он находится в невалидном состоянии.
     */
    public static Object instantiateEmpty(Class<?> type) {
        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать " + type.getName()
                    + " конструктором без параметров", e);
        }
    }

    static String decapitalize(String name) {
        // Правило java.beans: URL -> URL, Name -> name (две заглавные подряд не трогаем)
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}

package ru.sprbut.m10.lombok;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 59: «Изменение исходного кода (хак AST, как Lombok)».
 * <p>
 * Проверяем это рефлексией: в исходнике {@link CustomerEntity} нет ни одного
 * метода, а в байткоде их два десятка. Обычный annotation processor так не умеет —
 * штатное API позволяет только <b>создавать новые файлы</b>. Lombok же
 * встраивается в компилятор и правит внутреннее дерево разбора уже существующего
 * класса.
 * <p>
 * Практические следствия этого хака:
 * <ul>
 *   <li>IDE нужен отдельный плагин — иначе она не видит методов, которых нет
 *       в тексте;</li>
 *   <li>Lombok завязан на внутренности javac и ломается при смене мажорной
 *       версии JDK раньше других инструментов;</li>
 *   <li>зато результат — обычный байткод: ни рефлексии, ни прокси в runtime.</li>
 * </ul>
 */
public final class LombokUnderTheHood {

    private LombokUnderTheHood() {
    }

    /** Публичные методы, реально существующие в байткоде класса. */
    public static List<String> generatedMethodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .map(Method::getName)
                .sorted()
                .distinct()
                .toList();
    }

    /** Арности конструкторов — по ним видно {@code @NoArgsConstructor} и {@code @AllArgsConstructor}. */
    public static List<Integer> constructorArities(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors())
                .map(Constructor::getParameterCount)
                .sorted()
                .toList();
    }

    /** Все ли поля класса объявлены {@code final} — признак {@code @Value}. */
    public static boolean allFieldsAreFinal(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !f.isSynthetic() && !Modifier.isStatic(f.getModifiers()))
                .allMatch(f -> Modifier.isFinal(f.getModifiers()));
    }

    /** Есть ли у класса хотя бы один сеттер. */
    public static boolean hasSetters(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .anyMatch(m -> m.getName().startsWith("set") && m.getParameterCount() == 1);
    }

    /** Уровень доступа метода — {@code @Setter(AccessLevel.PROTECTED)} видно отсюда. */
    public static String accessLevelOf(Class<?> type, String methodName) {
        Method method = Arrays.stream(type.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет метода " + methodName
                        + "; есть только " + generatedMethodNames(type)));
        int mods = method.getModifiers();
        if (Modifier.isPublic(mods)) {
            return "public";
        }
        if (Modifier.isProtected(mods)) {
            return "protected";
        }
        if (Modifier.isPrivate(mods)) {
            return "private";
        }
        return "package-private";
    }

    /**
     * Соответствует ли класс соглашению JavaBeans (модуль 02).
     * {@code @Data} — соответствует, {@code @Accessors(fluent = true)} — уже нет.
     */
    public static boolean looksLikeJavaBean(Class<?> type) {
        boolean hasNoArgCtor = Arrays.stream(type.getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
        boolean hasPrefixedGetters = Arrays.stream(type.getDeclaredMethods())
                .anyMatch(m -> (m.getName().startsWith("get") || m.getName().startsWith("is"))
                        && m.getParameterCount() == 0);
        return hasNoArgCtor && hasPrefixedGetters;
    }
}

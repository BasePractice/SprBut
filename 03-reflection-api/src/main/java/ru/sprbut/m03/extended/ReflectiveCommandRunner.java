package ru.sprbut.m03.extended;

import ru.sprbut.m03.MethodApi;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * <b>Расширенный пример модуля 03.</b>
 * <p>
 * Мини-движок команд: строка вида
 * <pre>ru.sprbut.m03.model.Order(A-1,100)#addLines(10,20)</pre>
 * превращается в реальный вызов метода. Внутри задействованы <i>все</i> узлы
 * карты Reflection API (СХЕМА 1, слайд 27) сразу:
 * <ul>
 *   <li>{@code Class} — загрузка типа по имени через {@code Class.forName};</li>
 *   <li>{@code Constructor} — выбор конструктора по количеству и типам аргументов;</li>
 *   <li>{@code Method} — поиск метода, разбор сигнатуры, поддержка varargs;</li>
 *   <li>{@code Parameter} — типы, по которым конвертируются строковые аргументы;</li>
 *   <li>{@code Modifier} — отсев недоступных и абстрактных членов;</li>
 *   <li>{@code Array} — упаковка хвоста аргументов в varargs-массив.</li>
 * </ul>
 * Это ровно тот механизм, на котором работают {@code spring-shell}, JMX-операции
 * и маршрутизация запросов в Spring MVC.
 */
public final class ReflectiveCommandRunner {

    private ReflectiveCommandRunner() {
    }

    /** Результат выполнения команды вместе с расшифровкой того, что было выбрано. */
    public record Invocation(String type, String constructorUsed, String methodSignature, Object result) {
    }

    /**
     * Выполняет команду.
     *
     * @param command {@code fqcn(ctorArg,...)#method(arg,...)}; часть с конструктором
     *                можно опустить — тогда будет использован конструктор без параметров
     */
    public static Invocation run(String command) {
        int hash = command.indexOf('#');
        if (hash < 0) {
            throw new IllegalArgumentException("Ожидался формат 'Класс#метод(...)', получено: " + command);
        }
        Spec targetSpec = Spec.parse(command.substring(0, hash).trim());
        Spec methodSpec = Spec.parse(command.substring(hash + 1).trim());

        Class<?> type = loadClass(targetSpec.name());
        Constructor<?> ctor = selectConstructor(type, targetSpec.args().size());
        Object instance = instantiate(ctor, targetSpec.args());

        Method method = selectMethod(type, methodSpec.name(), methodSpec.args().size());
        Object[] args = bindArguments(method, methodSpec.args());
        Object result = invoke(method, instance, args);

        return new Invocation(type.getSimpleName(), describe(ctor), MethodApi.signature(method), result);
    }

    // --- Class ---------------------------------------------------------------

    static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Класс не найден: " + name, e);
        }
    }

    // --- Constructor ---------------------------------------------------------

    /**
     * Из всех конструкторов нужной арности выбираем тот, чьи типы поддерживает
     * конвертер; при равенстве предпочитаем более доступный (public раньше protected).
     */
    static Constructor<?> selectConstructor(Class<?> type, int arity) {
        if (Modifier.isAbstract(type.getModifiers()) || type.isInterface()) {
            throw new IllegalArgumentException("Нельзя создать экземпляр " + type.getSimpleName()
                    + ": это абстрактный тип");
        }
        return Arrays.stream(type.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == arity)
                .filter(c -> Arrays.stream(c.getParameterTypes()).allMatch(ArgumentConverter::supports))
                .min(Comparator.comparingInt(ReflectiveCommandRunner::accessRank))
                .orElseThrow(() -> new IllegalArgumentException("У " + type.getSimpleName()
                        + " нет пригодного конструктора с " + arity + " аргументами"));
    }

    private static Object instantiate(Constructor<?> ctor, List<String> rawArgs) {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = ArgumentConverter.convert(rawArgs.get(i), paramTypes[i]);
        }
        ctor.setAccessible(true);
        try {
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw unwrap(e);
        }
    }

    // --- Method --------------------------------------------------------------

    /**
     * Поиск метода по имени и числу аргументов. varargs-метод подходит, если
     * передано не меньше аргументов, чем обязательных параметров.
     */
    static Method selectMethod(Class<?> type, String name, int argCount) {
        List<Method> candidates = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.isBridge() || m.isSynthetic()) {
                    continue;
                }
                if (matchesArity(m, argCount)) {
                    candidates.add(m);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("У " + type.getSimpleName() + " нет метода '" + name
                    + "' с " + argCount + " аргументами");
        }
        // Точное совпадение арности предпочтительнее varargs-раскрытия
        return candidates.stream()
                .min(Comparator
                        .comparingInt((Method m) -> m.isVarArgs() ? 1 : 0)
                        .thenComparingInt(ReflectiveCommandRunner::accessRank))
                .orElseThrow();
    }

    private static boolean matchesArity(Method method, int argCount) {
        if (method.isVarArgs()) {
            return argCount >= method.getParameterCount() - 1;
        }
        return method.getParameterCount() == argCount;
    }

    /**
     * Конвертация аргументов по типам параметров. Для varargs хвост списка
     * упаковывается в массив через {@link Array#newInstance} — обычным
     * {@code new} это сделать нельзя, тип элемента известен только в runtime.
     */
    static Object[] bindArguments(Method method, List<String> rawArgs) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (!method.isVarArgs()) {
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = ArgumentConverter.convert(rawArgs.get(i), paramTypes[i]);
            }
            return args;
        }

        int fixed = paramTypes.length - 1;
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < fixed; i++) {
            args[i] = ArgumentConverter.convert(rawArgs.get(i), paramTypes[i]);
        }
        Class<?> componentType = paramTypes[fixed].getComponentType();
        int tailLength = rawArgs.size() - fixed;
        Object tail = Array.newInstance(componentType, tailLength);
        for (int i = 0; i < tailLength; i++) {
            Array.set(tail, i, ArgumentConverter.convert(rawArgs.get(fixed + i), componentType));
        }
        args[fixed] = tail;
        return args;
    }

    private static Object invoke(Method method, Object instance, Object[] args) {
        method.setAccessible(true);
        try {
            return method.invoke(Modifier.isStatic(method.getModifiers()) ? null : instance, args);
        } catch (ReflectiveOperationException e) {
            throw unwrap(e);
        }
    }

    // --- Modifier ------------------------------------------------------------

    private static int accessRank(java.lang.reflect.Member member) {
        int mods = member.getModifiers();
        if (Modifier.isPublic(mods)) {
            return 0;
        }
        if (Modifier.isProtected(mods)) {
            return 1;
        }
        if (Modifier.isPrivate(mods)) {
            return 3;
        }
        return 2;
    }

    static String describe(Constructor<?> ctor) {
        return ctor.getDeclaringClass().getSimpleName() + "("
                + Arrays.stream(ctor.getParameterTypes()).map(Class::getSimpleName)
                .reduce((a, b) -> a + ", " + b).orElse("") + ")";
    }

    private static RuntimeException unwrap(ReflectiveOperationException e) {
        Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
        if (cause instanceof RuntimeException re) {
            return re;
        }
        // checked-исключение из вызванного метода отдаём как есть, обёрнутым
        return new CommandFailedException(cause.getMessage(), cause);
    }

    /** Ошибка выполнения самой команды — в отличие от ошибки её разбора. */
    public static class CommandFailedException extends RuntimeException {
        public CommandFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Разобранная часть команды: имя и список сырых аргументов.
     */
    record Spec(String name, List<String> args) {

        static Spec parse(String raw) {
            int open = raw.indexOf('(');
            if (open < 0) {
                return new Spec(raw, List.of());
            }
            if (!raw.endsWith(")")) {
                throw new IllegalArgumentException("Не закрыта скобка в: " + raw);
            }
            String name = raw.substring(0, open).trim();
            String inside = raw.substring(open + 1, raw.length() - 1).trim();
            if (inside.isEmpty()) {
                return new Spec(name, List.of());
            }
            List<String> args = Arrays.stream(inside.split(","))
                    .map(String::trim)
                    .toList();
            return new Spec(name, args);
        }
    }
}

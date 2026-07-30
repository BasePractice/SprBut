package ru.sprbut.m03;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 23 (СХЕМА 1): {@link Method} — сигнатура метода целиком.
 * <p>
 * Возвращаемый тип, типы параметров, объявленные исключения, флаги
 * {@code varargs}/{@code default}/{@code bridge}. Именно из этих данных Spring MVC
 * решает, что подставить в аргументы контроллера.
 */
public final class MethodApi {

    private MethodApi() {
    }

    public static Class<?> returnType(Method method) {
        return method.getReturnType();
    }

    public static boolean isVoid(Method method) {
        return method.getReturnType() == void.class;
    }

    public static List<String> parameterTypeNames(Method method) {
        return Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .toList();
    }

    /**
     * {@link Parameter} даёт не только тип, но и имя параметра — но лишь если класс
     * скомпилирован с флагом {@code -parameters}. Иначе имена будут {@code arg0, arg1}.
     * В этом проекте флаг включён в корневом pom.xml.
     */
    public static List<String> parameterNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(Parameter::getName)
                .toList();
    }

    /** Объявленные checked-исключения — то, что стоит после {@code throws}. */
    public static List<String> declaredExceptions(Method method) {
        return Arrays.stream(method.getExceptionTypes())
                .map(Class::getSimpleName)
                .toList();
    }

    /**
     * varargs в байткоде — обычный параметр-массив плюс отдельный флаг.
     * Поэтому {@code getParameterTypes()} для {@code addLines(BigDecimal...)}
     * вернёт {@code BigDecimal[]}.
     */
    public static boolean isVarArgs(Method method) {
        return method.isVarArgs();
    }

    /** default-метод интерфейса: у него есть тело, и он не абстрактный. */
    public static boolean isDefault(Method method) {
        return method.isDefault();
    }

    /**
     * Синтетический bridge-метод компилятор создаёт при сужении типа возврата
     * или переопределении обобщённого метода. Фреймворкам его нужно отфильтровывать,
     * иначе один метод «находится» дважды.
     */
    public static boolean isBridge(Method method) {
        return method.isBridge();
    }

    /** Компактная подпись метода — удобно для сообщений об ошибках и логов. */
    public static String signature(Method method) {
        return method.getReturnType().getSimpleName() + " " + method.getName()
                + "(" + String.join(", ", parameterTypeNames(method)) + ")";
    }
}

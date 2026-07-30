package ru.sprbut.m03;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 23 (СХЕМА 1): {@link Method} — сигнатура метода целиком.
 * <p>
 * Возвращаемый тип, типы и имена параметров, объявленные исключения, флаги
 * {@code varargs}, {@code default} и {@code bridge}. Именно из этих данных
 * Spring MVC решает, что подставить в аргументы метода контроллера.
 */
public final class MethodSignature {

    private final Method method;

    public MethodSignature(Method method) {
        this.method = method;
    }

    /**
     * Тип возвращаемого значения.
     */
    public Class<?> returnType() {
        return this.method.getReturnType();
    }

    /**
     * Ничего не возвращает ли метод.
     */
    public boolean voidResult() {
        return this.method.getReturnType() == void.class;
    }

    /**
     * Типы параметров в порядке объявления.
     */
    public List<String> parameterTypes() {
        return Arrays.stream(this.method.getParameterTypes())
            .map(Class::getSimpleName)
            .toList();
    }

    /**
     * Имена параметров — но только если класс скомпилирован с флагом
     * {@code -parameters}. Иначе здесь будут {@code arg0}, {@code arg1};
     * в этом проекте флаг включён в корневом {@code pom.xml}.
     */
    public List<String> parameterNames() {
        return Arrays.stream(this.method.getParameters())
            .map(Parameter::getName)
            .toList();
    }

    /**
     * Объявленные исключения — то, что стоит после {@code throws}.
     */
    public List<String> exceptions() {
        return Arrays.stream(this.method.getExceptionTypes())
            .map(Class::getSimpleName)
            .toList();
    }

    /**
     * Переменное число аргументов. В байткоде это обычный параметр-массив
     * плюс отдельный флаг, поэтому {@code getParameterTypes()} покажет массив.
     */
    public boolean varargs() {
        return this.method.isVarArgs();
    }

    /**
     * Default-метод интерфейса: у него есть тело, и он не абстрактный.
     */
    public boolean defaultMethod() {
        return this.method.isDefault();
    }

    /**
     * Синтетический bridge-метод, созданный компилятором при сужении типа возврата.
     * Фреймворкам его нужно отфильтровывать, иначе один метод находится дважды.
     */
    public boolean bridge() {
        return this.method.isBridge();
    }

    /**
     * Компактная подпись — для сообщений об ошибках и логов.
     */
    public String text() {
        return this.method.getReturnType().getSimpleName() + " " + this.method.getName()
            + "(" + String.join(", ", parameterTypes()) + ")";
    }
}

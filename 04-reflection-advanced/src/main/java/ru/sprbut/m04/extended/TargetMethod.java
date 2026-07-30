package ru.sprbut.m04.extended;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Метод реализации, соответствующий методу интерфейса.
 * <p>
 * Аннотации ищутся на методе <b>реализации</b>: у интерфейса их может не быть
 * вовсе. Ровно так же поступает Spring, разбирая {@code @Transactional} —
 * и ровно об этом забывают, когда вешают аннотацию на интерфейс и удивляются
 * тишине.
 */
public final class TargetMethod {

    private final Object target;

    private final Method declared;

    public TargetMethod(Object target, Method declared) {
        this.target = target;
        this.declared = declared;
    }

    /**
     * Метод реализации; если его нет — метод интерфейса как есть.
     */
    public Method method() {
        try {
            return this.target.getClass()
                .getMethod(this.declared.getName(), this.declared.getParameterTypes());
        } catch (NoSuchMethodException absent) {
            return this.declared;
        }
    }

    /**
     * Хэндл для быстрого вызова: доступ проверяется один раз, при создании,
     * а не на каждом вызове, как у {@code Method.invoke}.
     */
    public MethodHandle handle() {
        try {
            Method implementation = method();
            implementation.setAccessible(true);
            return MethodHandles.lookup().unreflect(implementation);
        } catch (ReflectiveOperationException denied) {
            throw new IllegalStateException(
                "Не удалось получить хэндл для " + this.declared.getName(), denied
            );
        }
    }
}

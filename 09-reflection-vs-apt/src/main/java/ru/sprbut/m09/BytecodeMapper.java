package ru.sprbut.m09;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Слайд 75: «Байткод (CGLIB, ByteBuddy): и то, и другое».
 * <p>
 * Третий механизм не сводится ни к рефлексии, ни к APT: класс <b>создаётся
 * заново</b> — на этапе сборки или прямо в runtime — и дальше работает
 * как обычный скомпилированный класс.
 * <ul>
 *   <li>гибкость рефлексии: решение принимается во время выполнения,
 *       по данным, которых не было при компиляции;</li>
 *   <li>скорость APT: сгенерированный байткод исполняется напрямую,
 *       без {@code Method.invoke} на каждый вызов;</li>
 *   <li>цена: класс появляется в runtime, поэтому в native image так нельзя
 *       (слайд 77) — там всё должно быть известно при сборке.</li>
 * </ul>
 * Именно этим механизмом Spring создаёт CGLIB-прокси, когда у бина нет
 * интерфейса (модуль 15).
 */
public final class BytecodeMapper {

    private final ClassLoader loader;

    public BytecodeMapper() {
        this(BytecodeMapper.class.getClassLoader());
    }

    public BytecodeMapper(ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Маппер, класс которого собран и загружен прямо сейчас.
     * <p>
     * В исходниках такого класса нет вовсе — он существует только в памяти
     * этой JVM.
     */
    public UserMapper mapper() {
        try {
            return (UserMapper) new ByteBuddy()
                .subclass(Object.class)
                .implement(UserMapper.class)
                .name("ru.sprbut.m09.bytebuddy.GeneratedUserMapper")
                .method(ElementMatchers.named("toDto"))
                .intercept(MethodDelegation.to(CopyInterceptor.class))
                .method(ElementMatchers.named("strategy"))
                .intercept(FixedValue.value("bytecode: класс собран и загружен в runtime"))
                .make()
                .load(this.loader, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Не удалось создать сгенерированный маппер", failure);
        }
    }

    /**
     * Подкласс конкретного класса с перехватом методов — то, что делает CGLIB,
     * когда у бина нет интерфейса.
     */
    public AuditService proxied() {
        new Intercepted().clear();
        try {
            return new ByteBuddy()
                .subclass(AuditService.class)
                .name("ru.sprbut.m09.bytebuddy.AuditService$$Enhanced")
                .method(ElementMatchers.isDeclaredBy(AuditService.class))
                .intercept(
                    MethodDelegation.to(LoggingInterceptor.class).andThen(SuperMethodCall.INSTANCE)
                )
                .make()
                .load(this.loader, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Не удалось создать CGLIB-подобный прокси", failure);
        }
    }
}

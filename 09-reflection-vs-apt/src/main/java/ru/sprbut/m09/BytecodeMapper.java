package ru.sprbut.m09;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Слайд 75: «Байткод (CGLIB, ByteBuddy): и то, и другое».
 * <p>
 * Третий механизм не сводится ни к рефлексии, ни к APT: класс <b>создаётся
 * заново</b> — либо на этапе сборки, либо прямо в runtime — и дальше работает
 * как обычный скомпилированный класс.
 * <ul>
 *   <li>гибкость рефлексии: решение принимается во время выполнения,
 *       по данным, которых не было при компиляции;</li>
 *   <li>скорость APT: сгенерированный байткод исполняется напрямую,
 *       без {@code Method.invoke} на каждый вызов;</li>
 *   <li>цена: класс появляется в runtime, поэтому в native image так нельзя
 *       (слайд 77) — там всё должно быть известно при сборке.</li>
 * </ul>
 * Именно этим механизмом Spring создаёт CGLIB-прокси, когда у бина
 * нет интерфейса (модуль 15).
 */
public final class BytecodeMapper {

    private BytecodeMapper() {
    }

    /**
     * Генерирует и загружает класс, реализующий {@link UserMapper}.
     * В исходниках такого класса нет — он собран из байткода при вызове.
     */
    public static UserMapper create() {
        try {
            Class<?> generated = new ByteBuddy()
                    .subclass(Object.class)
                    .implement(UserMapper.class)
                    .name("ru.sprbut.m09.bytebuddy.GeneratedUserMapper")
                    .method(ElementMatchers.named("toDto"))
                    .intercept(MethodDelegation.to(CopyInterceptor.class))
                    .method(ElementMatchers.named("strategy"))
                    .intercept(FixedValue.value("bytecode: класс собран и загружен в runtime"))
                    .make()
                    // WRAPPER — отдельный загрузчик, без обращения к sun.misc.Unsafe
                    .load(BytecodeMapper.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();

            return (UserMapper) generated.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать сгенерированный маппер", e);
        }
    }

    /** Тело метода {@code toDto} сгенерированного класса. */
    public static class CopyInterceptor {

        public static UserDto map(@Argument(0) UserEntity entity) {
            if (entity == null) {
                return null;
            }
            UserDto dto = new UserDto();
            dto.setId(entity.getId());
            dto.setFirstName(entity.getFirstName());
            dto.setLastName(entity.getLastName());
            dto.setAge(entity.getAge());
            dto.setActive(entity.isActive());
            return dto;
        }
    }

    // --- То, чего не может JDK-прокси ---------------------------------------

    /** Сервис <b>без интерфейса</b> — JDK-прокси такой класс проксировать не умеет. */
    public static class AuditService {

        public String record(String event) {
            return "записано: " + event;
        }

        public int size() {
            return 0;
        }
    }

    /** Журнал перехваченных вызовов. */
    public static final List<String> INTERCEPTED = new CopyOnWriteArrayList<>();

    /**
     * Создаёт подкласс конкретного класса и перехватывает его методы —
     * то, что в Spring делает CGLIB, когда у бина нет интерфейса.
     */
    public static AuditService proxyWithoutInterface() {
        INTERCEPTED.clear();
        try {
            return new ByteBuddy()
                    .subclass(AuditService.class)
                    .name("ru.sprbut.m09.bytebuddy.AuditService$$Enhanced")
                    .method(ElementMatchers.isDeclaredBy(AuditService.class))
                    .intercept(MethodDelegation.to(LoggingInterceptor.class)
                            .andThen(SuperMethodCall.INSTANCE))
                    .make()
                    .load(BytecodeMapper.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось создать CGLIB-подобный прокси", e);
        }
    }

    /** Записывает факт вызова, затем управление уходит в оригинальный метод. */
    public static class LoggingInterceptor {

        public static void before(@This Object self) {
            INTERCEPTED.add("вызов у " + self.getClass().getSimpleName());
        }
    }
}

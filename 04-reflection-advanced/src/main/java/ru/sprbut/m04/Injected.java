package ru.sprbut.m04;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает параметр как точку внедрения — учебный аналог {@code @Autowired}
 * вместе с {@code @Qualifier}.
 * <p>
 * {@code @Target(PARAMETER)} здесь не формальность: контейнер ищет метаданные
 * именно на параметрах, потому что подставлять значения он будет тоже в них.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Injected {

    /**
     * Имя нужного бина; пустое — подбор по типу.
     */
    String value() default "";
}

package ru.sprbut.m07.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает класс для включения в сгенерированный реестр.
 * <p>
 * Это compile-time аналог {@code @Component}: вместо сканирования classpath
 * в runtime список собирается на этапе компиляции. Именно так работает
 * Spring AOT и Micronaut/Quarkus — и ровно поэтому им не нужна рефлексия
 * при старте (слайд 195, модуль 22).
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Registered {

    /** Логическое имя в реестре. Пустое — имя класса с маленькой буквы. */
    String value() default "";
}

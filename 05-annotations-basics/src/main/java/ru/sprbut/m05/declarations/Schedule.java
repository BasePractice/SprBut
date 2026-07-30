package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 44: «{@code @Repeatable} — может быть применена одна и та же аннотация
 * много раз к одному участку кода».
 * <p>
 * Механика: аннотация помечается {@code @Repeatable(Контейнер.class)}, а контейнер
 * обязан иметь элемент {@code value()} типа «массив повторяемой аннотации».
 * Компилятор сам заворачивает несколько экземпляров в контейнер — поэтому
 * {@code getAnnotation(Schedule.class)} при двух и более вхождениях вернёт
 * {@code null}, и читать надо через {@code getAnnotationsByType}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Schedules.class)
public @interface Schedule {

    String cron();

    String zone() default "UTC";
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.aspects;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Аспект над контроллером: последний, кто трогает аргументы.
 *
 * <p>Резолвер собирает один аргумент и не видит остальных. Аспект стоит
 * вокруг вызова метода, когда все аргументы уже собраны, и может
 * заменить любые из них: {@code proceed} принимает новый массив.</p>
 *
 * <p>Цена — место в цепочке. К моменту вызова аспекта {@code @Valid} уже
 * отработал в резолвере, так что правило {@code @Size} посчитает пробелы,
 * которые аспект потом срежет. И аспект требует прокси: класс
 * контроллера без интерфейсов прокси-объект может только расширить, и
 * {@code final} контроллер с аспектом не поднимется, как не поднялась бы
 * {@code final} конфигурация с {@code proxyBeanMethods}.</p>
 *
 * @since 1.0
 */
@Aspect
@Component
public final class TrimmingAspect {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TrimmingAspect() {
        // нечего инициализировать
    }

    /**
     * Вызов метода с обрезанными строковыми аргументами.
     * @param point Точка вызова
     * @return Результат метода
     * @throws Throwable Если метод бросил исключение
     * @checkstyle IllegalThrowsCheck (6 lines)
     */
    @Around("@within(ru.sprbut.m28.aspects.Trimmed)")
    public Object trim(final ProceedingJoinPoint point) throws Throwable {
        return point.proceed(
            Arrays.stream(point.getArgs()).map(TrimmingAspect::trimmed).toArray()
        );
    }

    private static Object trimmed(final Object arg) {
        final Object trimmed;
        if (arg instanceof String text) {
            trimmed = text.trim();
        } else {
            trimmed = arg;
        }
        return trimmed;
    }
}

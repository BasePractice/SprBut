/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.audit;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * Аспект, превращающий {@link Audited} из метки в поведение.
 *
 * <p>Spring оборачивает бин прокси, прокси перехватывает вызов, аспект читает
 * аннотацию рефлексией через {@code MethodSignature} и пишет в журнал.
 * Тот же механизм стоит за {@code @Transactional} и {@code @Cacheable}.</p>
 *
 * <p>Отсюда же и главное ограничение прокси: вызов соседнего метода изнутри
 * того же объекта идёт мимо обёртки и в журнал не попадает.</p>
 *
 * @since 1.0
 */
@Aspect
@Component
public final class AuditAspect {

    /**
     * Журнал событий.
     */
    private final AuditTrail trail;

    /**
     * Основной конструктор.
     * @param trail Журнал событий
     */
    public AuditAspect(final AuditTrail trail) {
        this.trail = trail;
    }

    /**
     * Записывает операцию после её успешного выполнения.
     * Совет вокруг обязан объявлять {@code Throwable}: сигнатуру диктует
     * контракт {@code ProceedingJoinPoint.proceed}.
     * @param point Точка соединения
     * @return Результат работы целевого метода
     * @throws Throwable Если целевой метод завершился ошибкой
     * @checkstyle IllegalThrowsCheck (6 lines)
     */
    @Around("@annotation(ru.sprbut.m27.audit.Audited)")
    public Object around(final ProceedingJoinPoint point) throws Throwable {
        final Object result = point.proceed();
        this.trail.record(AuditAspect.operation(point));
        return result;
    }

    private static String operation(final ProceedingJoinPoint point) {
        final Method method = AopUtils.getMostSpecificMethod(
            ((MethodSignature) point.getSignature()).getMethod(),
            point.getTarget().getClass()
        );
        final Audited audited = method.getAnnotation(Audited.class);
        final String name;
        if (audited == null || audited.value().isBlank()) {
            name = method.getName();
        } else {
            name = audited.value();
        }
        return name;
    }
}

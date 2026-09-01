/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.extended;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Расширенный пример модуля 15 (часть 1).</b>
 *
 * <p>Аспект, реализующий {@link Retryable}. Это точная модель того, как устроены
 * {@code @Transactional} и {@code @Cacheable}: аннотация — только метаданные,
 * всё поведение живёт в аспекте, который читает её через рефлексию.</p>
 *
 * <p>{@code @Order} важен: аспектов на одном методе может быть несколько, и порядок
 * их наложения определяет семантику. Транзакция должна быть <b>внутри</b> ретрая,
 * иначе повторная попытка пойдёт в уже сломанной транзакции.</p>
 *
 * @since 1.0
 */
@Aspect
@Component
@Order(1)
public class RetryAspect {

    /**
     * Журнал.
     */
    private final List<String> log = new ArrayList<>(0);

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RetryAspect() {
        // нечего инициализировать
    }

    /**
     * Журнал.
     * @return Журнал
     */
    public List<String> log() {
        return List.copyOf(this.log);
    }

    /**
     * Очистка.
     */
    public void clear() {
        this.log.clear();
    }

    /**
     * Число попыток.
     * @param method Метод
     * @return Число попыток
     */
    public long attemptsOf(final String method) {
        return this.log.stream().filter(e -> e.contains(":" + method + ":")).count();
    }

    /**
     * Pointcut по аннотации, а не по имени метода: так аспект не зависит
     * от структуры пакетов.
     * @param joinPoint Значение {@code joinPoint}
     * @return Pointcut по аннотации, а не по имени метода: так аспект не зависит от структуры пакетов
     */
    @Around("@annotation(ru.sprbut.m15.extended.Retryable)")
    public Object retry(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Retryable annotation = signature.getMethod().getAnnotation(Retryable.class);
        final int attempts = annotation == null ? 1 : annotation.attempts();
        Throwable last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                final Object result = joinPoint.proceed();
                this.log.add("success:" + signature.getName() + ":попытка" + attempt);
                return result;
            } catch (final Throwable e) {
                last = e;
                this.log.add("fail:" + signature.getName() + ":попытка" + attempt);
            }
        }
        this.log.add("exhausted:" + signature.getName());
        throw last;
    }
}

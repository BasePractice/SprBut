/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// контракт AspectJ: around-advice должен объявлять throws Throwable
// и пропускать через себя любое исключение цели
// @checkstyle IllegalThrowsCheck disable
// @checkstyle IllegalCatchCheck disable
package ru.sprbut.m15.extended;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
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
        final String marker = String.format(":%s:", method);
        return this.log.stream().filter(entry -> entry.contains(marker)).count();
    }

    /**
     * Pointcut по аннотации, а не по имени метода: так аспект не зависит
     * от структуры пакетов.
     * @param point Точка соединения
     * @return Результат первой удачной попытки
     * @throws Throwable Исключение последней попытки, если удачной не случилось
     */
    @Around("@annotation(ru.sprbut.m15.extended.Retryable)")
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Object retry(final ProceedingJoinPoint point) throws Throwable {
        final MethodSignature signature = (MethodSignature) point.getSignature();
        final Retryable annotation = signature.getMethod().getAnnotation(Retryable.class);
        final int attempts;
        if (annotation == null) {
            attempts = 1;
        } else {
            attempts = annotation.attempts();
        }
        Throwable last = null;
        Object result = null;
        int attempt = 0;
        while (result == null && attempt < attempts) {
            attempt += 1;
            try {
                result = point.proceed();
                this.log.add(
                    String.format("success:%s:попытка%d", signature.getName(), attempt)
                );
            } catch (final Throwable error) {
                last = error;
                this.log.add(String.format("fail:%s:попытка%d", signature.getName(), attempt));
            }
        }
        if (result == null) {
            this.log.add(String.format("exhausted:%s", signature.getName()));
            throw last;
        }
        return result;
    }
}

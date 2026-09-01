/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайды 121–126 (СХЕМА 9): «Прокси вокруг бина, класс не меняется».
 *
 * <p>Аспект — это описание того, <b>что</b> сделать и <b>где</b>. «Где» задаётся
 * pointcut-выражением, «что» — телом advice-метода. Сам целевой класс при этом
 * не меняется ни одной строчкой: поведение живёт в обёртке.</p>
 *
 * <p>Механически это ровно тот же {@code InvocationHandler} из модуля 04, только
 * прокси создаёт не ваш код, а {@code BeanPostProcessor} на шаге 6 жизненного
 * цикла (модуль 14).</p>
 *
 * @since 1.0
 */
@Aspect
@Component
public class AuditAspect {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AuditAspect() {
        // нечего инициализировать
    }

    /**
     * Журнал.
     */
    private final List<String> log = new ArrayList<>();

    /**
     * Именованный pointcut: выражение можно переиспользовать.
     */
    @Pointcut("execution(* ru.sprbut.m15.aop.*Service.*(..))")
    public void anyServiceMethod() {
    }

    /**
     * Advice «до вызова»: результат метода изменить нельзя, можно только наблюдать.
     * @param joinPoint Значение {@code joinPoint}
     */
    @Before("anyServiceMethod()")
    public void before(final org.aspectj.lang.JoinPoint joinPoint) {
        this.log.add("before:" + joinPoint.getSignature().getName());
    }

    /**
     * Advice «вокруг вызова» — самый мощный: решает, вызывать ли цель вообще,
     * может подменить аргументы и результат. Именно так устроены
     * {@code @Transactional} и {@code @Cacheable}.
     * @param joinPoint Значение {@code joinPoint}
     * @return Advice «вокруг вызова» — самый мощный: решает, вызывать ли цель вообще, может подменить аргументы и результат. Именно так устроены {@code @Transactional} и {@code @Cacheable}
     */
    @Around("execution(* ru.sprbut.m15.aop.*Service.calculate(..))")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        this.log.add("around-start:" + joinPoint.getSignature().getName());
        try {
            final Object result = joinPoint.proceed();
            this.log.add("around-end:" + joinPoint.getSignature().getName());
            return result;
        } catch (final Throwable e) {
            this.log.add("around-error:" + joinPoint.getSignature().getName());
            throw e;
        }
    }

    /**
     * Advice «после исключения»: исключение не гасится, только наблюдается.
     * @param error Ошибка
     */
    @AfterThrowing(pointcut = "anyServiceMethod()", throwing = "error")
    public void afterThrowing(final Throwable error) {
        this.log.add("afterThrowing:" + error.getClass().getSimpleName());
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
}

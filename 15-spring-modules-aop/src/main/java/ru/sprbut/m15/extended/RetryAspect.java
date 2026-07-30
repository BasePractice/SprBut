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
 * <p>
 * Аспект, реализующий {@link Retryable}. Это точная модель того, как устроены
 * {@code @Transactional} и {@code @Cacheable}: аннотация — только метаданные,
 * всё поведение живёт в аспекте, который читает её через рефлексию.
 * <p>
 * {@code @Order} важен: аспектов на одном методе может быть несколько, и порядок
 * их наложения определяет семантику. Транзакция должна быть <b>внутри</b> ретрая,
 * иначе повторная попытка пойдёт в уже сломанной транзакции.
 */
@Aspect
@Component
@Order(1)
public class RetryAspect {

    private final List<String> log = new ArrayList<>();

    /**
     * Pointcut по аннотации, а не по имени метода: так аспект не зависит
     * от структуры пакетов.
     */
    @Around("@annotation(ru.sprbut.m15.extended.Retryable)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Retryable annotation = signature.getMethod().getAnnotation(Retryable.class);
        int attempts = annotation == null ? 1 : annotation.attempts();

        Throwable last = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                Object result = joinPoint.proceed();
                log.add("success:" + signature.getName() + ":попытка" + attempt);
                return result;
            } catch (Throwable e) {
                last = e;
                log.add("fail:" + signature.getName() + ":попытка" + attempt);
            }
        }
        log.add("exhausted:" + signature.getName());
        throw last;
    }

    public List<String> log() {
        return List.copyOf(log);
    }

    public void clear() {
        log.clear();
    }

    public long attemptsOf(String method) {
        return log.stream().filter(e -> e.contains(":" + method + ":")).count();
    }
}

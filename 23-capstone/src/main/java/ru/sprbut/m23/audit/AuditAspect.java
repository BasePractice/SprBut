package ru.sprbut.m23.audit;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * Аспект, превращающий {@link Audited} из метки в поведение.
 * <p>
 * Spring оборачивает бин прокси, прокси перехватывает вызов, аспект читает
 * аннотацию рефлексией через {@code MethodSignature} и пишет в журнал.
 * Тот же механизм стоит за {@code @Transactional} и {@code @Cacheable}.
 * <p>
 * Отсюда же и главное ограничение прокси: вызов соседнего метода изнутри
 * того же объекта идёт мимо обёртки и в журнал не попадает.
 */
@Aspect
@Component
public final class AuditAspect {

    private final AuditTrail trail;

    public AuditAspect(AuditTrail trail) {
        this.trail = trail;
    }

    /**
     * Записывает операцию после её успешного выполнения.
     */
    @Around("@annotation(ru.sprbut.m23.audit.Audited)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();
        this.trail.record(operation(point));
        return result;
    }

    /**
     * Имя операции из аннотации на методе <b>реализации</b>.
     * <p>
     * Ловушка JDK-прокси: {@code MethodSignature.getMethod()} отдаёт метод
     * интерфейса, где никакой аннотации нет. Без
     * {@code getMostSpecificMethod} аспект молча писал бы в журнал имена
     * методов вместо заданных имён операций.
     */
    private String operation(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = AopUtils.getMostSpecificMethod(
            signature.getMethod(), point.getTarget().getClass()
        );
        Audited audited = method.getAnnotation(Audited.class);
        if (audited == null || audited.value().isBlank()) {
            return method.getName();
        }
        return audited.value();
    }
}

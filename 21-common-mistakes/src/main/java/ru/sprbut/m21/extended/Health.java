package ru.sprbut.m21.extended;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m21.Diagnosis;

/**
 * <b>Расширенный пример модуля 21.</b>
 * <p>
 * Здоровье конфигурации: поднимает контекст в изоляции и, если тот падает,
 * превращает стектрейс на двести строк в два предложения — что сломалось
 * и что делать.
 * <p>
 * Это учебная модель {@code FailureAnalyzer} из Spring Boot. Настоящий
 * регистрируется в {@code META-INF/spring.factories} и работает ровно так же:
 * ловит исключение старта, распознаёт знакомый тип и печатает человеческий
 * разбор вместо стектрейса.
 * <p>
 * Контекст здесь — ресурс, а не поле: он закрывается сразу после проверки,
 * поэтому диагност не держит ни одного живого бина.
 */
public final class Health {

    private final Class<?> config;

    public Health(Class<?> config) {
        this.config = config;
    }

    /**
     * Диагноз конфигурации: {@link Healthy}, если контекст собрался,
     * иначе разбор причины падения.
     */
    public Diagnosis diagnosis() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(this.config)) {
            context.getBeanDefinitionCount();
            return new Healthy();
        } catch (RuntimeException failure) {
            return new Failure(failure).diagnosis();
        }
    }
}

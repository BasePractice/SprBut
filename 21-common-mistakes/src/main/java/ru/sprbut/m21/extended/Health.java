/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.extended;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m21.Diagnosis;

/**
 * <b>Расширенный пример модуля 21.</b>
 *
 * <p>Здоровье конфигурации: поднимает контекст в изоляции и, если тот падает,
 * превращает стектрейс на двести строк в два предложения — что сломалось
 * и что делать.</p>
 *
 * <p>Это учебная модель {@code FailureAnalyzer} из Spring Boot. Настоящий
 * регистрируется в {@code META-INF/spring.factories} и работает ровно так же:
 * ловит исключение старта, распознаёт знакомый тип и печатает человеческий
 * разбор вместо стектрейса.</p>
 *
 * <p>Контекст здесь — ресурс, а не поле: он закрывается сразу после проверки,
 * поэтому диагност не держит ни одного живого бина.</p>
 *
 * @since 1.0
 */
public final class Health {

    /**
     * Конфигурация, здоровье которой проверяется.
     */
    private final Class<?> config;

    /**
     * Основной конструктор.
     * @param config Конфигурация, здоровье которой проверяется
     */
    public Health(final Class<?> config) {
        this.config = config;
    }

    /**
     * Диагноз конфигурации: {@link Healthy}, если контекст собрался,
     * иначе разбор причины падения.
     * Ловить {@code RuntimeException} здесь и есть предмет разговора:
     * контейнер заворачивает любую поломку старта именно в него.
     * @return Диагноз конфигурации
     * @checkstyle IllegalCatchCheck (14 lines)
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Diagnosis diagnosis() {
        Diagnosis verdict;
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(this.config)
        ) {
            context.getBeanDefinitionCount();
            verdict = new Healthy();
        } catch (final RuntimeException failure) {
            verdict = new Failure(failure).diagnosis();
        }
        return verdict;
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.sprbut.m23.audit.AuditTrail;
import ru.sprbut.m23.config.TrackerProperties;

/**
 * Работа, которую нужно сделать один раз после того, как контекст собран.
 *
 * <p>{@code ApplicationRunner} — предпоследний шаг запуска со слайда «Запуск»:
 * контекст уже обновлён, все {@code @PostConstruct} отработали, до
 * {@code ApplicationReadyEvent} остаётся один шаг. Здесь уже безопасно
 * обращаться к любым бинам.</p>
 *
 * @since 1.0
 */
@Component
public final class TrackerRunner implements ApplicationRunner {

    /**
     * Настройки.
     */
    private final TrackerProperties settings;

    /**
     * Журнал событий.
     */
    private final AuditTrail trail;

    /**
     * Основной конструктор.
     * @param settings Настройки
     * @param trail Журнал событий
     */
    public TrackerRunner(final TrackerProperties settings, final AuditTrail trail) {
        this.settings = settings;
        this.trail = trail;
    }

    @Override
    public void run(final ApplicationArguments args) {
        this.trail.record("startup:" + this.settings.title());
    }
}

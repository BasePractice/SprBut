/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Внешняя конфигурация трекера.
 *
 * <p>Привязка через конструктор делает свойства неизменяемыми: значения приезжают
 * один раз при старте, сеттеров нет, подменить их в рантайме нельзя.
 * Это тот редкий случай, когда требование фреймворка и Elegant Objects
 * тянут в одну сторону.</p>
 *
 * <p>Приоритет источников фиксирован: значения по умолчанию отсюда, затем
 * {@code application.yaml}, затем профиль, затем переменные окружения,
 * системные {@code -D} и, выше всего, аргументы командной строки.</p>
 *
 * @since 1.0
 */
@ConfigurationProperties(prefix = "tracker")
public final class TrackerProperties {

    /**
     * Название.
     */
    private final String title;

    /**
     * Предел.
     */
    private final int limit;

    /**
     * Аудит.
     */
    private final boolean audit;

    /**
     * Основной конструктор.
     *
     * <p>Значения по умолчанию подставляются прямо здесь: свойства могут
     * прийти пустыми, а объект обязан родиться готовым.</p>
     *
     * @param title Название
     * @param limit Предел
     * @param audit Аудит
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public TrackerProperties(final String title, final Integer limit, final Boolean audit) {
        this.title = Objects.requireNonNullElse(title, "SprBut Tracker");
        this.limit = Objects.requireNonNullElse(limit, 100);
        this.audit = Objects.requireNonNullElse(audit, Boolean.TRUE);
    }

    /**
     * Отображаемое имя установки.
     * @return Отображаемое имя установки
     */
    public String title() {
        return this.title;
    }

    /**
     * Максимальное количество открытых задач.
     * @return Максимальное количество открытых задач
     */
    public int limit() {
        return this.limit;
    }

    /**
     * Включён ли журнал аудита.
     * @return Включён ли журнал аудита
     */
    public boolean audit() {
        return this.audit;
    }
}

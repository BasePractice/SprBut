package ru.sprbut.m23.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Внешняя конфигурация трекера.
 * <p>
 * Привязка через конструктор делает свойства неизменяемыми: значения приезжают
 * один раз при старте, сеттеров нет, подменить их в рантайме нельзя.
 * Это тот редкий случай, когда требование фреймворка и Elegant Objects
 * тянут в одну сторону.
 * <p>
 * Приоритет источников фиксирован: значения по умолчанию отсюда, затем
 * {@code application.yaml}, затем профиль, затем переменные окружения,
 * системные {@code -D} и, выше всего, аргументы командной строки.
 */
@ConfigurationProperties(prefix = "tracker")
public final class TrackerProperties {

    private final String title;

    private final int limit;

    private final boolean audit;

    public TrackerProperties(String title, Integer limit, Boolean audit) {
        this.title = title == null ? "SprBut Tracker" : title;
        this.limit = limit == null ? 100 : limit;
        this.audit = audit == null || audit;
    }

    /**
     * Отображаемое имя установки.
     */
    public String title() {
        return this.title;
    }

    /**
     * Максимальное количество открытых задач.
     */
    public int limit() {
        return this.limit;
    }

    /**
     * Включён ли журнал аудита.
     */
    public boolean audit() {
        return this.audit;
    }
}

package ru.sprbut.m23.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.sprbut.m23.audit.AuditTrail;
import ru.sprbut.m23.config.TrackerProperties;

/**
 * Работа, которую нужно сделать один раз после того, как контекст собран.
 * <p>
 * {@code ApplicationRunner} — предпоследний шаг запуска со слайда «Запуск»:
 * контекст уже обновлён, все {@code @PostConstruct} отработали, до
 * {@code ApplicationReadyEvent} остаётся один шаг. Здесь уже безопасно
 * обращаться к любым бинам.
 */
@Component
public final class TrackerRunner implements ApplicationRunner {

    private final TrackerProperties settings;

    private final AuditTrail trail;

    public TrackerRunner(TrackerProperties settings, AuditTrail trail) {
        this.settings = settings;
        this.trail = trail;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.trail.record("startup:" + this.settings.title());
    }
}

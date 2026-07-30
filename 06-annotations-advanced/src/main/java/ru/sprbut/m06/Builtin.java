package ru.sprbut.m06;

import java.lang.annotation.Annotation;

/**
 * Служебная ли это мета-аннотация самого языка.
 * <p>
 * Обход мета-аннотаций обязан их исключать: {@code @Retention} помечена
 * {@code @Retention}, {@code @Target} — {@code @Target}, и отчёт утонул бы
 * в шуме ещё до того, как дошёл до прикладных аннотаций.
 */
public final class Builtin {

    private final Class<? extends Annotation> type;

    public Builtin(Class<? extends Annotation> type) {
        this.type = type;
    }

    /**
     * Принадлежит ли аннотация самому языку.
     */
    public boolean yes() {
        return this.type.getName().startsWith("java.lang.annotation.")
            || this.type.getName().startsWith("jdk.internal.");
    }
}

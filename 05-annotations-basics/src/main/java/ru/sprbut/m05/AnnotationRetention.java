package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Слайд 40: {@code @Retention{SOURCE, CLASS, RUNTIME}}.
 * <p>
 * Единственный практический вывод: <b>если аннотация не помечена
 * {@code @Retention(RUNTIME)}, ни Spring, ни ваш собственный код её не увидят.</b>
 * Это причина примерно половины вопросов «почему моя аннотация не работает».
 * <p>
 * Умолчание коварно: без {@code @Retention} политика равна {@code CLASS},
 * то есть аннотация попадёт в байткод, но останется невидимой для рефлексии.
 */
public final class AnnotationRetention {

    private final Class<? extends Annotation> annotation;

    public AnnotationRetention(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Политика хранения; {@code CLASS}, если {@code @Retention} не указан.
     */
    public RetentionPolicy policy() {
        Retention retention = this.annotation.getAnnotation(Retention.class);
        return retention == null ? RetentionPolicy.CLASS : retention.value();
    }

    /**
     * Видна ли аннотация рефлексии.
     */
    public boolean visible() {
        return policy() == RetentionPolicy.RUNTIME;
    }
}

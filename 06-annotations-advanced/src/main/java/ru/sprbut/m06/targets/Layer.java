package ru.sprbut.m06.targets;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Слой, объявленный мета-аннотацией {@link Stereotype} на другой аннотации.
 * <p>
 * Читается ровно так же, как аннотация обычного класса, — и это не совпадение:
 * тип аннотации для рефлексии такой же {@code Class}, как всякий другой.
 */
public final class Layer {

    private final Class<? extends Annotation> annotation;

    public Layer(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Имя слоя, если аннотация помечена стереотипом.
     */
    public Optional<String> name() {
        return Optional.ofNullable(this.annotation.getAnnotation(Stereotype.class))
            .map(Stereotype::layer);
    }
}

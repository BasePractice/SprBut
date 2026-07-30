package ru.sprbut.m05;

import ru.sprbut.m05.declarations.Retentions;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 40: три политики хранения и то, что из них следует на практике.
 * <p>
 * Единственный практический вывод: <b>если аннотация не помечена
 * {@code @Retention(RUNTIME)}, ни Spring, ни ваш собственный код её не увидят.</b>
 * Это причина примерно половины вопросов «почему моя аннотация не работает».
 */
public final class RetentionVisibility {

    private RetentionVisibility() {
    }

    /** Политика хранения аннотации; {@code CLASS}, если {@code @Retention} не указан. */
    public static RetentionPolicy policyOf(Class<? extends Annotation> annotation) {
        Retention retention = annotation.getAnnotation(Retention.class);
        return retention == null ? RetentionPolicy.CLASS : retention.value();
    }

    /** Видна ли аннотация рефлексии. */
    public static boolean visibleAtRuntime(Class<? extends Annotation> annotation) {
        return policyOf(annotation) == RetentionPolicy.RUNTIME;
    }

    /** Что реально видно на элементе — независимо от того, что там написано в коде. */
    public static List<String> visibleAnnotations(java.lang.reflect.AnnotatedElement element) {
        return Arrays.stream(element.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .sorted()
                .toList();
    }

    /**
     * Класс с тремя аннотациями разной политики на каждом элементе.
     * В runtime из трёх видна ровно одна.
     */
    @Retentions.SourceLevel
    @Retentions.ClassLevel
    @Retentions.RuntimeLevel
    @Retentions.DefaultRetention
    @SuppressWarnings("unused")
    public static class TripleAnnotated {

        @Retentions.SourceLevel
        @Retentions.ClassLevel
        @Retentions.RuntimeLevel
        private String field;

        @Retentions.SourceLevel
        @Retentions.ClassLevel
        @Retentions.RuntimeLevel
        public void method() {
        }
    }
}

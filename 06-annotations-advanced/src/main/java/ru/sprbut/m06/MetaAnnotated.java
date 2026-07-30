package ru.sprbut.m06;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Слайд 55: «{@code @RestController} = {@code @Controller} + {@code @ResponseBody}».
 * <p>
 * Важное следствие композиции: штатный {@code getAnnotation(Controller.class)}
 * на классе с {@code @RestController} вернёт {@code null} — язык мета-аннотации
 * не раскрывает. Искать их приходится рекурсивно вручную, что Spring и делает
 * в {@code AnnotatedElementUtils}.
 * <p>
 * Обход обязан помнить, где был: {@code @Retention} помечена {@code @Retention},
 * и наивная рекурсия зациклилась бы на первом же шаге.
 */
public final class MetaAnnotated {

    private final Class<?> type;

    public MetaAnnotated(Class<?> type) {
        this.type = type;
    }

    /**
     * Наивная проверка: только то, что написано прямо на классе.
     */
    public boolean direct(Class<? extends Annotation> annotation) {
        return this.type.isAnnotationPresent(annotation);
    }

    /**
     * Рекурсивный поиск, в том числе через мета-аннотации, — упрощённый
     * {@code AnnotatedElementUtils.hasAnnotation}.
     */
    public boolean deep(Class<? extends Annotation> annotation) {
        return search(this.type.getAnnotations(), annotation, new HashSet<>());
    }

    /**
     * Полная цепочка мета-аннотаций вглубь — то, что нужно печатать при отладке
     * «почему мой бин не подхватился».
     */
    public List<String> chain() {
        List<String> collected = new ArrayList<>();
        collect(this.type.getAnnotations(), collected, new HashSet<>(), 0);
        return List.copyOf(collected);
    }

    private boolean search(
        Annotation[] annotations,
        Class<? extends Annotation> target,
        Set<Class<? extends Annotation>> visited
    ) {
        for (Annotation each : annotations) {
            Class<? extends Annotation> candidate = each.annotationType();
            if (candidate.equals(target)) {
                return true;
            }
            if (new Builtin(candidate).yes() || !visited.add(candidate)) {
                continue;
            }
            if (search(candidate.getAnnotations(), target, visited)) {
                return true;
            }
        }
        return false;
    }

    private void collect(
        Annotation[] annotations,
        List<String> sink,
        Set<Class<? extends Annotation>> visited,
        int depth
    ) {
        for (Annotation each : annotations) {
            Class<? extends Annotation> candidate = each.annotationType();
            if (new Builtin(candidate).yes() || !visited.add(candidate)) {
                continue;
            }
            sink.add("  ".repeat(depth) + "@" + candidate.getSimpleName());
            collect(candidate.getAnnotations(), sink, visited, depth + 1);
        }
    }
}

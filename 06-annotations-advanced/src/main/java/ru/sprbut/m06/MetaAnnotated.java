/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Слайд 55: «{@code @RestController} = {@code @Controller} + {@code @ResponseBody}».
 *
 * <p>Важное следствие композиции: штатный {@code getAnnotation(Controller.class)}
 * на классе с {@code @RestController} вернёт {@code null} — язык мета-аннотации
 * не раскрывает. Искать их приходится рекурсивно вручную, что Spring и делает
 * в {@code AnnotatedElementUtils}.</p>
 *
 * <p>Обход должен помнить, где был: {@code @Retention} помечена {@code @Retention},
 * и наивная рекурсия зациклилась бы на первом же шаге.</p>
 *
 * @since 1.0
 */
public final class MetaAnnotated {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public MetaAnnotated(final Class<?> type) {
        this.type = type;
    }

    /**
     * Наивная проверка: только то, что написано прямо на классе.
     * @param annotation Аннотация
     * @return Наивная проверка: только то, что написано прямо на классе
     */
    public boolean direct(final Class<? extends Annotation> annotation) {
        return this.type.isAnnotationPresent(annotation);
    }

    /**
     * Рекурсивный поиск, в том числе через мета-аннотации, — упрощённый
     * {@code AnnotatedElementUtils.hasAnnotation}.
     * @param annotation Аннотация
     * @return Признак того, что аннотация найдена
     */
    public boolean deep(final Class<? extends Annotation> annotation) {
        return this.search(this.type.getAnnotations(), annotation, new HashSet<>(0));
    }

    /**
     * Полная цепочка мета-аннотаций вглубь — то, что нужно печатать при отладке
     * «почему мой бин не подхватился».
     * @return Полная цепочка мета-аннотаций
     */
    public List<String> chain() {
        final List<String> collected = new ArrayList<>(0);
        this.collect(this.type.getAnnotations(), collected, new HashSet<>(0), 0);
        return List.copyOf(collected);
    }

    private boolean search(
        final Annotation[] annotations,
        final Class<? extends Annotation> target,
        final Set<Class<? extends Annotation>> visited
    ) {
        boolean found = false;
        for (final Annotation each : annotations) {
            final Class<? extends Annotation> candidate = each.annotationType();
            if (candidate.equals(target)) {
                found = true;
            } else if (!new Builtin(candidate).yes() && visited.add(candidate)) {
                found = this.search(candidate.getAnnotations(), target, visited);
            }
            if (found) {
                break;
            }
        }
        return found;
    }

    private void collect(
        final Annotation[] annotations,
        final List<String> sink,
        final Set<Class<? extends Annotation>> visited,
        final int depth
    ) {
        for (final Annotation each : annotations) {
            final Class<? extends Annotation> candidate = each.annotationType();
            if (new Builtin(candidate).yes() || !visited.add(candidate)) {
                continue;
            }
            sink.add(String.format("%s@%s", "  ".repeat(depth), candidate.getSimpleName()));
            this.collect(candidate.getAnnotations(), sink, visited, depth + 1);
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m17.extended;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Все аннотации, до которых можно дойти по цепочке мета-аннотаций,
 * включая саму исходную.
 *
 * <p>Обход должен отсекать аннотации самой Java: {@code @Retention} помечена
 * {@code @Retention}, и без этой проверки он не закончится никогда.</p>
 *
 * @since 1.0
 */
public final class Expanded {

    /**
     * Аннотация.
     */
    private final Class<? extends Annotation> annotation;

    /**
     * Основной конструктор.
     * @param annotation Аннотация
     */
    public Expanded(final Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Имена всех достижимых аннотаций в алфавитном порядке.
     * @return Имена всех достижимых аннотаций в алфавитном порядке
     */
    public Set<String> names() {
        final Set<Class<? extends Annotation>> visited = new HashSet<>(0);
        final Set<String> found = new TreeSet<>();
        final Deque<Class<? extends Annotation>> queue = new ArrayDeque<>(0);
        queue.add(this.annotation);
        while (!queue.isEmpty()) {
            final Class<? extends Annotation> current = queue.poll();
            if (Expanded.builtin(current) || !visited.add(current)) {
                continue;
            }
            found.add(String.format("@%s", current.getSimpleName()));
            for (final Annotation meta : current.getAnnotations()) {
                queue.add(meta.annotationType());
            }
        }
        return Set.copyOf(found);
    }

    /**
     * К каким базовым аннотациям сводится эта — без неё самой.
     * @return К каким базовым аннотациям сводится эта — без неё самой
     */
    public List<String> parts() {
        return this.names().stream()
            .filter(name -> !name.equals(String.format("@%s", this.annotation.getSimpleName())))
            .sorted()
            .toList();
    }

    /**
     * Сводится ли аннотация к {@code @Component}, то есть стереотип ли это.
     * @return Сводится ли аннотация к {@code @Component}, то есть стереотип ли это
     */
    public boolean stereotype() {
        return this.names().contains("@Component");
    }

    /**
     * Наглядное объяснение состава — для лекции и для отладки.
     * @return Наглядное объяснение состава — для лекции и для отладки
     */
    public String explain() {
        final List<String> parts = this.parts();
        final String explanation;
        if (parts.isEmpty()) {
            explanation = String.format("@%s — базовая аннотация", this.annotation.getSimpleName());
        } else {
            explanation = String.format(
                "@%s = %s", this.annotation.getSimpleName(), String.join(" + ", parts)
            );
        }
        return explanation;
    }

    private static boolean builtin(final Class<? extends Annotation> type) {
        return type.getName().startsWith("java.lang.annotation.")
            || type.getName().startsWith("jdk.internal.");
    }
}

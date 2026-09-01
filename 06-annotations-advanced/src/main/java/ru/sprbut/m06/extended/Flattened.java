/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.sprbut.m06.Builtin;

/**
 * Все аннотации элемента, включая мета-аннотации, одним плоским списком.
 *
 * <p>Первое, что стоит напечатать при вопросе «что вообще навешано на этот класс
 * и почему он ведёт себя не так»: цепочки композиций в исходниках не видны
 * вовсе, а здесь видны все сразу.</p>
 *
 * @since 1.0
 */
public final class Flattened {

    /**
     * Элемент.
     */
    private final AnnotatedElement element;

    /**
     * Основной конструктор.
     * @param element Элемент
     */
    public Flattened(final AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена всех аннотаций вглубь, в алфавитном порядке.
     * @return Имена всех аннотаций вглубь, в алфавитном порядке
     */
    public List<String> names() {
        final List<String> collected = new ArrayList<>();
        final Deque<Annotation> queue = new ArrayDeque<>(List.of(this.element.getAnnotations()));
        final Set<Class<? extends Annotation>> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            final Annotation each = queue.poll();
            final Class<? extends Annotation> type = each.annotationType();
            if (new Builtin(type).yes() || !visited.add(type)) {
                continue;
            }
            collected.add("@" + type.getSimpleName());
            queue.addAll(List.of(type.getAnnotations()));
        }
        collected.sort(String::compareTo);
        return List.copyOf(collected);
    }
}

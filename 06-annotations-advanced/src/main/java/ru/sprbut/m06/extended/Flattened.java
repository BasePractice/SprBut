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
 * <p>
 * Первое, что стоит напечатать при вопросе «что вообще навешано на этот класс
 * и почему он ведёт себя не так»: цепочки композиций в исходниках не видны
 * вовсе, а здесь видны все сразу.
 */
public final class Flattened {

    private final AnnotatedElement element;

    public Flattened(AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена всех аннотаций вглубь, в алфавитном порядке.
     */
    public List<String> names() {
        List<String> collected = new ArrayList<>();
        Deque<Annotation> queue = new ArrayDeque<>(List.of(this.element.getAnnotations()));
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Annotation each = queue.poll();
            Class<? extends Annotation> type = each.annotationType();
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

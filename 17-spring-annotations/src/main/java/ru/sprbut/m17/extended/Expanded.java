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
 * <p>
 * Обход обязан отсекать аннотации самой Java: {@code @Retention} помечена
 * {@code @Retention}, и без этой проверки он не закончится никогда.
 */
public final class Expanded {

    private final Class<? extends Annotation> annotation;

    public Expanded(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Имена всех достижимых аннотаций в алфавитном порядке.
     */
    public Set<String> names() {
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        Set<String> found = new TreeSet<>();
        Deque<Class<? extends Annotation>> queue = new ArrayDeque<>();
        queue.add(this.annotation);
        while (!queue.isEmpty()) {
            Class<? extends Annotation> current = queue.poll();
            if (builtin(current) || !visited.add(current)) {
                continue;
            }
            found.add("@" + current.getSimpleName());
            for (Annotation meta : current.getAnnotations()) {
                queue.add(meta.annotationType());
            }
        }
        return Set.copyOf(found);
    }

    /**
     * К каким базовым аннотациям сводится эта — без неё самой.
     */
    public List<String> parts() {
        return names().stream()
            .filter(name -> !name.equals("@" + this.annotation.getSimpleName()))
            .sorted()
            .toList();
    }

    /**
     * Сводится ли аннотация к {@code @Component}, то есть стереотип ли это.
     */
    public boolean stereotype() {
        return names().contains("@Component");
    }

    /**
     * Наглядное объяснение состава — для лекции и для отладки.
     */
    public String explain() {
        List<String> parts = parts();
        if (parts.isEmpty()) {
            return "@" + this.annotation.getSimpleName() + " — базовая аннотация";
        }
        return "@" + this.annotation.getSimpleName() + " = " + String.join(" + ", parts);
    }

    private boolean builtin(Class<? extends Annotation> type) {
        return type.getName().startsWith("java.lang.annotation.")
            || type.getName().startsWith("jdk.internal.");
    }
}

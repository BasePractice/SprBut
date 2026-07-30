package ru.sprbut.m17.extended;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>Расширенный пример модуля 17.</b>
 * <p>
 * Каталог аннотаций Spring, который <b>раскрывает композиции сам</b> — тем же
 * приёмом, что и {@code MergedAnnotationScanner} из модуля 06.
 * <p>
 * Смысл в том, что список аннотаций со слайдов 140–149 выглядит длинным
 * и разрозненным, а на деле сводится к нескольким базовым:
 * <pre>
 *   @RestController          = @Controller + @ResponseBody
 *   @Controller / @Service / @Repository = @Component + смысл (+ поведение)
 *   @SpringBootApplication   = @SpringBootConfiguration (= @Configuration)
 *                            + @EnableAutoConfiguration
 *                            + @ComponentScan
 * </pre>
 * Каталог это доказывает, а не пересказывает.
 */
public final class AnnotationCatalog {

    private AnnotationCatalog() {
    }

    /** Аннотации самой Java — их надо отсекать, иначе обход зациклится. */
    static boolean isJavaBuiltin(Class<? extends Annotation> type) {
        return type.getName().startsWith("java.lang.annotation.")
                || type.getName().startsWith("jdk.internal.");
    }

    /**
     * Все аннотации, до которых можно дойти по цепочке мета-аннотаций,
     * включая саму исходную.
     */
    public static Set<String> expand(Class<? extends Annotation> annotation) {
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        Set<String> names = new java.util.TreeSet<>();
        Deque<Class<? extends Annotation>> queue = new ArrayDeque<>();
        queue.add(annotation);

        while (!queue.isEmpty()) {
            Class<? extends Annotation> current = queue.poll();
            if (isJavaBuiltin(current) || !visited.add(current)) {
                continue;
            }
            names.add("@" + current.getSimpleName());
            for (Annotation meta : current.getAnnotations()) {
                queue.add(meta.annotationType());
            }
        }
        return names;
    }

    /** Является ли аннотация стереотипом — то есть сводится ли к {@code @Component}. */
    public static boolean isStereotype(Class<? extends Annotation> annotation) {
        return expand(annotation).contains("@Component");
    }

    /** К каким базовым аннотациям сводится указанная. */
    public static List<String> reducesTo(Class<? extends Annotation> annotation) {
        return expand(annotation).stream()
                .filter(name -> !name.equals("@" + annotation.getSimpleName()))
                .toList();
    }

    /** Полный справочник модуля: аннотация → что она на самом деле включает. */
    public static Map<String, Set<String>> catalog() {
        Map<String, Set<String>> catalog = new LinkedHashMap<>();
        for (Class<? extends Annotation> annotation : List.of(
                Component.class, Service.class, Repository.class, Controller.class,
                RestController.class, Configuration.class, Bean.class, ComponentScan.class,
                SpringBootApplication.class)) {
            catalog.put("@" + annotation.getSimpleName(), expand(annotation));
        }
        return catalog;
    }

    /** Все стереотипы из справочника. */
    public static List<String> stereotypes() {
        List<String> result = new ArrayList<>();
        for (Class<? extends Annotation> annotation : List.of(
                Component.class, Service.class, Repository.class, Controller.class,
                RestController.class, Configuration.class, Bean.class)) {
            if (isStereotype(annotation)) {
                result.add("@" + annotation.getSimpleName());
            }
        }
        return result;
    }

    /** Наглядное объяснение состава аннотации — для лекции и для отладки. */
    public static String explain(Class<? extends Annotation> annotation) {
        List<String> parts = reducesTo(annotation);
        if (parts.isEmpty()) {
            return "@" + annotation.getSimpleName() + " — базовая аннотация";
        }
        return "@" + annotation.getSimpleName() + " = " + String.join(" + ", parts);
    }
}

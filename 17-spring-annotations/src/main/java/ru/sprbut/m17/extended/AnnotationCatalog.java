package ru.sprbut.m17.extended;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>Расширенный пример модуля 17.</b>
 * <p>
 * Каталог аннотаций Spring, который <b>раскрывает композиции сам</b> — тем же
 * приёмом, что и сканер из модуля 06.
 * <p>
 * Смысл в том, что список аннотаций со слайдов 140–149 выглядит длинным
 * и разрозненным, а на деле сводится к нескольким базовым:
 * <pre>
 *   &#64;RestController        = &#64;Controller + &#64;ResponseBody
 *   &#64;Service / &#64;Repository = &#64;Component + смысл (+ поведение)
 *   &#64;SpringBootApplication = &#64;Configuration + &#64;EnableAutoConfiguration + &#64;ComponentScan
 * </pre>
 * Каталог это доказывает, а не пересказывает.
 */
public final class AnnotationCatalog {

    private final List<Class<? extends Annotation>> annotations;

    public AnnotationCatalog() {
        this(List.of(
            Component.class, Service.class, Repository.class, Controller.class,
            RestController.class, Configuration.class, Bean.class, ComponentScan.class,
            SpringBootApplication.class
        ));
    }

    public AnnotationCatalog(List<Class<? extends Annotation>> annotations) {
        this.annotations = List.copyOf(annotations);
    }

    /**
     * Справочник «аннотация — что она на самом деле включает».
     */
    public Map<String, Set<String>> all() {
        Map<String, Set<String>> catalog = new LinkedHashMap<>();
        for (Class<? extends Annotation> annotation : this.annotations) {
            catalog.put("@" + annotation.getSimpleName(), new Expanded(annotation).names());
        }
        return Map.copyOf(catalog);
    }

    /**
     * Аннотации, сводящиеся к {@code @Component}.
     */
    public List<String> stereotypes() {
        List<String> found = new ArrayList<>();
        for (Class<? extends Annotation> annotation : this.annotations) {
            if (new Expanded(annotation).stereotype()) {
                found.add("@" + annotation.getSimpleName());
            }
        }
        return List.copyOf(found);
    }
}

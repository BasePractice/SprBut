/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
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
 *
 * <p>Каталог аннотаций Spring, который <b>раскрывает композиции сам</b> — тем же
 * приёмом, что и сканер из модуля 06.</p>
 *
 * <p>Смысл в том, что список аннотаций со слайдов 140–149 выглядит длинным
 * и разрозненным, а на деле сводится к нескольким базовым:
 * <pre>
 * &#64;RestController        = &#64;Controller + &#64;ResponseBody
 * &#64;Service / &#64;Repository = &#64;Component + смысл (+ поведение)
 * &#64;SpringBootApplication = &#64;Configuration + &#64;EnableAutoConfiguration + &#64;ComponentScan
 * </pre>
 * Каталог это доказывает, а не пересказывает.</p>
 *
 * @since 1.0
 */
public final class AnnotationCatalog {

    /**
     * Значение {@code annotations}.
     */
    private final List<Class<? extends Annotation>> annotations;

    /**
     * Основной конструктор.
     */
    public AnnotationCatalog() {
        this(List.of(
            Component.class, Service.class, Repository.class, Controller.class,
            RestController.class, Configuration.class, Bean.class, ComponentScan.class,
            SpringBootApplication.class
        ));
    }

    /**
     * Основной конструктор.
     * @param annotations Значение {@code annotations}
     */
    public AnnotationCatalog(final List<Class<? extends Annotation>> annotations) {
        this.annotations = List.copyOf(annotations);
    }

    /**
     * Справочник «аннотация — что она на самом деле включает».
     * @return Справочник «аннотация — что она на самом деле включает»
     */
    public Map<String, Set<String>> all() {
        final Map<String, Set<String>> catalog = new LinkedHashMap<>();
        for (final Class<? extends Annotation> annotation : this.annotations) {
            catalog.put("@" + annotation.getSimpleName(), new Expanded(annotation).names());
        }
        return Map.copyOf(catalog);
    }

    /**
     * Аннотации, сводящиеся к {@code @Component}.
     * @return Аннотации, сводящиеся к {@code @Component}
     */
    public List<String> stereotypes() {
        final List<String> found = new ArrayList<>();
        for (final Class<? extends Annotation> annotation : this.annotations) {
            if (new Expanded(annotation).stereotype()) {
                found.add("@" + annotation.getSimpleName());
            }
        }
        return List.copyOf(found);
    }
}

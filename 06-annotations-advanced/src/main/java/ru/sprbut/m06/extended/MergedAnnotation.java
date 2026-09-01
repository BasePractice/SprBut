/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ru.sprbut.m06.Builtin;

/**
 * <b>Расширенный пример модуля 06.</b>
 *
 * <p>Сканер «слитых» аннотаций — рабочая мини-версия
 * {@code AnnotatedElementUtils.findMergedAnnotation} из Spring. Он делает то,
 * чего <b>не делает сам язык</b>:
 * <ul>
 * <li>находит аннотацию через произвольно длинную цепочку мета-аннотаций
 * ({@code @GetJson} → {@code @GetMapping} → {@code @RequestMapping});</li>
 * <li>сливает значения: заданное в композитной аннотации переопределяет
 * значение мета-аннотации, а незаданное берётся из {@code default};</li>
 * <li>уважает {@link AliasFor} — явное указание, какой элемент какой переопределяет.</li>
 * </ul>
 * Обход идёт <b>в ширину</b>, а не в глубину: ближайшее к элементу объявление
 * должно выигрывать у дальнего, иначе значение из мета-аннотации затирало бы
 * то, что человек написал руками.</p>
 *
 * <p>Это и есть ответ на вопрос «почему {@code @RestController} ведёт себя как
 * {@code @Controller}»: не потому, что так устроена Java, а потому,
 * что так написан читающий код.</p>
 *
  * @param <A> Параметр типа
 * @since 1.0
 */
public final class MergedAnnotation<A extends Annotation> {

    /**
     * Элемент.
     */
    private final AnnotatedElement element;

    /**
     * Целевой объект.
     */
    private final Class<A> target;

    /**
     * Основной конструктор.
     * @param element Элемент
     * @param target Целевой объект
     */
    public MergedAnnotation(final AnnotatedElement element, final Class<A> target) {
        this.element = element;
        this.target = target;
    }

    /**
     * Найденная аннотация со слитыми значениями.
     * @return Найденная аннотация со слитыми значениями
     */
    public Optional<Merged> find() {
        return this.search(this.element.getAnnotations());
    }

    /**
     * Поиск среди набора аннотаций — общая часть для элемента и для иерархии.
     * @param roots Значение {@code roots}
     * @return Поиск среди набора аннотаций — общая часть для элемента и для иерархии
     */
    Optional<Merged> search(final Annotation[] roots) {
        final Deque<Step> queue = new ArrayDeque<>();
        final Set<Class<? extends Annotation>> visited = new HashSet<>();
        for (final Annotation each : roots) {
            if (!new Builtin(each.annotationType()).yes()) {
                queue.add(new Step(each, List.of()));
            }
        }
        while (!queue.isEmpty()) {
            final Step step = queue.poll();
            final Class<? extends Annotation> type = step.annotation().annotationType();
            if (!visited.add(type)) {
                continue;
            }
            final List<Annotation> path = new ArrayList<>(step.path());
            path.add(step.annotation());
            if (type.equals(this.target)) {
                return Optional.of(this.merged(path));
            }
            for (final Annotation meta : type.getAnnotations()) {
                if (!new Builtin(meta.annotationType()).yes()) {
                    queue.add(new Step(meta, path));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Слияние значений вдоль пути: базой служат значения самой целевой
     * аннотации, поверх ложатся переопределения композитных — от дальней
     * к ближайшей, чтобы ближайшая выиграла.
     * @param path Путь
     * @return Слияние значений вдоль пути: базой служат значения самой целевой аннотации, поверх ложатся переопределения композитных — от дальней к ближайшей, чтобы ближайшая выиграла
     */
    private Merged merged(final List<Annotation> path) {
        final Map<String, Object> attributes =
            new LinkedHashMap<>(new RawAttributes(path.get(path.size() - 1)).map());
        for (int index = path.size() - 2; index >= 0; index--) {
            this.override(path.get(index), attributes);
        }
        return new Merged(
            this.target,
            attributes,
            path.stream().map(each -> "@" + each.annotationType().getSimpleName()).toList()
        );
    }

    /**
     * Переопределения, которые композитная аннотация вносит в целевую.
     *
     * <p>Два правила, оба как в Spring: элемент с {@link AliasFor} переопределяет
     * названный там элемент; одноимённый элемент переопределяет одноимённый,
     * но только если его значение отличается от {@code default} — иначе
     * «незаданный» элемент затирал бы осмысленное значение.</p>
     * @param source Источник
      * @param annotation Параметр типа
      * @param path Параметр типа
     * @param attributes Значение {@code attributes}
     */
    private void override(final Annotation source, final Map<String, Object> attributes) {
        final RawAttributes raw = new RawAttributes(source);
        for (final Method element : source.annotationType().getDeclaredMethods()) {
            final Object value = raw.value(element);
            final AliasFor alias = element.getAnnotation(AliasFor.class);
            if (alias != null && alias.annotation().equals(this.target)) {
                attributes.put(this.aliased(alias, element, attributes), value);
                continue;
            }
            if (attributes.containsKey(element.getName())
                && !Objects.deepEquals(value, element.getDefaultValue())) {
                attributes.put(element.getName(), value);
            }
        }
    }

    private String aliased(final AliasFor alias, final Method element, final Map<String, Object> attributes) {
        final String name = alias.attribute().isBlank() ? element.getName() : alias.attribute();
        if (!attributes.containsKey(name)) {
            throw new IllegalStateException(
                "@AliasFor указывает на несуществующий элемент '" + name
                    + "' аннотации @" + this.target.getSimpleName()
            );
        }
        return name;
    }

    /**
     * Шаг обхода: аннотация и путь, которым до неё дошли.
     */
    private record Step(Annotation annotation, List<Annotation> path) {
    }
}

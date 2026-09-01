/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// фикстура существует ради полей, которые читает рефлексия
// @checkstyle VisibilityModifierCheck disable
package ru.sprbut.m01;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.extended.JsonIgnore;
import ru.sprbut.m01.extended.JsonProperty;

/**
 * Слайд 9: чтение аннотаций через рефлексию.
 * @since 1.0
 */
@DisplayName("Слайд 9: чтение аннотаций через рефлексию")
final class AnnotationsTest {

    @Test
    @DisplayName("isAnnotationPresent проверяет наличие аннотации")
    void detectsPresentAnnotation() {
        MatcherAssert.assertThat(
            "present annotation cannot be detected",
            new Annotations(new Declared(Sample.class).field("id")).has(JsonProperty.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("getAnnotation отдаёт саму аннотацию со значениями элементов")
    void readsAnnotationValue() {
        MatcherAssert.assertThat(
            "annotation element value cannot be read",
            new Annotations(new Declared(Sample.class).field("id"))
                .find(JsonProperty.class)
                .map(JsonProperty::value)
                .orElseThrow(),
            Matchers.equalTo("account_id")
        );
    }

    @Test
    @DisplayName("поле без аннотации даёт пустой Optional, а не исключение")
    void dontFailOnMissingAnnotation() {
        MatcherAssert.assertThat(
            "missing annotation cannot yield an empty optional",
            new Annotations(new Declared(Sample.class).field("plain"))
                .find(JsonProperty.class)
                .isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("все runtime-аннотации элемента перечисляются разом")
    void listsRuntimeAnnotations() {
        MatcherAssert.assertThat(
            "runtime annotations cannot be listed together",
            new Annotations(new Declared(Sample.class).field("id")).names(),
            Matchers.contains("JsonIgnore", "JsonProperty")
        );
    }

    @Test
    @DisplayName("аннотация с RetentionPolicy.SOURCE в runtime не существует вовсе")
    void dontSeeSourceRetention() {
        MatcherAssert.assertThat(
            "source retained annotation cannot stay invisible at runtime",
            new Annotations(new Declared(Sample.class).field("invisible")).names(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("аннотации читаются с любого AnnotatedElement, в том числе с самой аннотации")
    void readsFromAnyAnnotatedElement() {
        MatcherAssert.assertThat(
            "annotation on an annotation cannot be read the same way",
            new Annotations(JsonProperty.class).has(Retention.class),
            Matchers.equalTo(true)
        );
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    private @interface SourceOnly {
    }

    @SuppressWarnings("unused")
    private static final class Sample {

        /**
         * Обычный вариант.
         */
        String plain;

        /**
         * Невидимый элемент.
         */
        @SourceOnly
        String invisible;

        /**
         * Идентификатор.
         */
        @JsonProperty("account_id")
        @JsonIgnore
        String id;
    }
}

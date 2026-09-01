/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.members;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайды 50–52: типы элементов и значения по умолчанию.
 * @since 1.0
 */
@DisplayName("Слайды 50–52: типы элементов и значения по умолчанию")
final class AnnotationMembersTest {

    @Test
    @DisplayName("обязателен только элемент без default")
    void listsRequiredElements() {
        MatcherAssert.assertThat(
            "element without default cannot be the only required one",
            new AnnotationMembers(Operation.class).required(),
            Matchers.contains("name")
        );
    }

    @Test
    @DisplayName("значение по умолчанию примитива читается отдельно от использования")
    void readsPrimitiveDefault() {
        MatcherAssert.assertThat(
            "primitive default cannot be read",
            new AnnotationMembers(Operation.class).defaults(),
            Matchers.hasEntry("timeout", 30)
        );
    }

    @Test
    @DisplayName("значение по умолчанию типа Class печатается коротким именем")
    void readsClassDefault() {
        MatcherAssert.assertThat(
            "class default cannot be printed readably",
            new AnnotationMembers(Operation.class).defaults(),
            Matchers.hasEntry("rollbackFor", "RuntimeException")
        );
    }

    @Test
    @DisplayName("вложенная аннотация по умолчанию тоже читается")
    void readsNestedAnnotationDefault() {
        MatcherAssert.assertThat(
            "nested annotation default cannot be read",
            new AnnotationMembers(Operation.class).defaults(),
            Matchers.hasEntry("retry", "@Retry")
        );
    }

    @Test
    @DisplayName("массив по умолчанию печатается содержимым, а не хэшем")
    void readsArrayDefault() {
        MatcherAssert.assertThat(
            "array default cannot be printed by its contents",
            new AnnotationMembers(Operation.class).defaults(),
            Matchers.hasEntry("allowed", "[DEFAULT]")
        );
    }

    @Test
    @DisplayName("минимальное использование берёт значения из умолчаний")
    void fallsBackToDefaults() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "minimal usage cannot fall back to the defaults",
            new AnnotationValues(
                Service.class.getMethod("withDefaults").getAnnotation(Operation.class)
            ).values(),
            Matchers.hasEntry("timeout", 30)
        );
    }

    @Test
    @DisplayName("заданное значение перебивает умолчание")
    void keepsExplicitValue() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "explicit value cannot override the default",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            Matchers.hasEntry("timeout", 5)
        );
    }

    @Test
    @DisplayName("enum читается своей константой")
    void readsEnumValue() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "enum element cannot be read as its constant",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            Matchers.hasEntry("isolation", Isolation.SERIALIZABLE)
        );
    }

    @Test
    @DisplayName("массив строк читается содержимым")
    void readsStringArray() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "string array cannot be read by its contents",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            Matchers.hasEntry("tags", "[critical, billing]")
        );
    }

    @Test
    @DisplayName("вложенная аннотация со своими значениями остаётся аннотацией")
    void readsNestedAnnotationValue() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "nested annotation value cannot stay an annotation",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values().get("retry"),
            Matchers.equalTo("@Retry")
        );
    }
}

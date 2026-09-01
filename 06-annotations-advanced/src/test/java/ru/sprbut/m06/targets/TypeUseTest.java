/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.targets;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайды 48–49: цели, появившиеся после Java 8.
 * @since 1.0
 */
@DisplayName("Слайды 48–49: цели, появившиеся после Java 8")
final class TypeUseTest {

    @Test
    @DisplayName("TYPE_USE читается с AnnotatedType, а не с поля")
    void readsAnnotationOnFieldType() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "type use annotation cannot be read from the annotated type",
            new TypeUse(Holder.class.getField("direct")).onType(),
            Matchers.contains("NonNull")
        );
    }

    @Test
    @DisplayName("обычный getAnnotations() этих аннотаций не видит вовсе")
    void dontSeeTypeUseOnField() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "plain field lookup cannot miss the type use annotation",
            Holder.class.getField("direct").getAnnotations().length,
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("аннотация внутри дженерика лежит на аргументе типа")
    void readsAnnotationInsideGenerics() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "annotation inside generics cannot be read from the type argument",
            new TypeUse(Holder.class.getField("insideGenerics")).onArguments(),
            Matchers.contains("NonNull")
        );
    }

    @Test
    @DisplayName("у непомеченного поля аннотаций типа нет")
    void reportsNoAnnotationsForPlainField() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "plain field cannot report an empty list",
            new TypeUse(Holder.class.getField("plain")).onArguments(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("TYPE_PARAMETER читается с объявления переменной типа")
    void readsTypeParameterAnnotation() {
        MatcherAssert.assertThat(
            "type parameter annotation cannot be read",
            new TypeParameters(Holder.class).names(0),
            Matchers.contains("Comparablish")
        );
    }

    @Test
    @DisplayName("RECORD_COMPONENT — отдельная ветка API")
    void readsRecordComponentAnnotation() {
        MatcherAssert.assertThat(
            "record component annotation cannot be read",
            new RecordColumn(UserRow.class, "id").name().orElseThrow(),
            Matchers.equalTo("user_id")
        );
    }

    @Test
    @DisplayName("несуществующий компонент record — понятная ошибка")
    void failsOnUnknownComponent() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new RecordColumn(UserRow.class, "nope").name()
        );
    }

    @Test
    @DisplayName("ANNOTATION_TYPE: мета-аннотация читается с аннотации как с класса")
    void readsMetaAnnotationOnAnnotation() {
        MatcherAssert.assertThat(
            "meta annotation cannot be read from an annotation type",
            new Layer(WebLayer.class).name().orElseThrow(),
            Matchers.equalTo("web")
        );
    }

    @Test
    @DisplayName("аннотация без стереотипа слоя не называет")
    void reportsNoLayerWithoutStereotype() {
        MatcherAssert.assertThat(
            "annotation without stereotype cannot report an empty layer",
            new Layer(NonNull.class).name().isEmpty(),
            Matchers.equalTo(true)
        );
    }
}

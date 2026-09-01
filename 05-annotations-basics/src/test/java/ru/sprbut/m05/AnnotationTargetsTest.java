/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;
import ru.sprbut.m05.declarations.Retentions;
import ru.sprbut.m05.samples.Annotated;
import ru.sprbut.m05.samples.TripleAnnotated;

/**
 * Слайды 39–40: @Target и @Retention.
 * @since 1.0
 */
@DisplayName("Слайды 39–40: @Target и @Retention")
final class AnnotationTargetsTest {

    @Test
    @DisplayName("@Target перечисляет разрешённые места")
    void listsAllowedTargets() {
        MatcherAssert.assertThat(
            "allowed targets cannot be read from the meta annotation",
            new AnnotationTargets(Level.class).allowed(),
            Matchers.hasItems(ElementType.TYPE, ElementType.FIELD, ElementType.METHOD)
        );
    }

    @Test
    @DisplayName("аннотация, разрешённая только на типах, поля не допускает")
    void restrictsToTypes() {
        MatcherAssert.assertThat(
            "type only annotation cannot be rejected for fields",
            new AnnotationTargets(Audited.class).fields(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("@Target — ограничение компилятора: в runtime аннотация есть где разрешили")
    void appliesAtCompileTime() {
        MatcherAssert.assertThat(
            "annotation cannot appear where the target allows it",
            new VisibleAnnotations(Annotated.class).names(),
            Matchers.hasItems("Level", "Marker")
        );
    }

    @Test
    @DisplayName("аннотация локальной переменной в class-файл не попадает вовсе")
    void dontKeepLocalVariableAnnotations() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "local variable annotation cannot vanish from the class file",
            new VisibleAnnotations(Annotated.class.getMethod("method", String.class)).names(),
            Matchers.not(Matchers.hasItem("SuppressWarnings"))
        );
    }

    @Test
    @DisplayName("без @Retention политика по умолчанию — CLASS")
    void defaultsToClassRetention() {
        MatcherAssert.assertThat(
            "missing retention cannot default to CLASS",
            new AnnotationRetention(Retentions.DefaultRetention.class).policy(),
            Matchers.equalTo(RetentionPolicy.CLASS)
        );
    }

    @Test
    @DisplayName("только RUNTIME видна рефлексии")
    void keepsRuntimeVisible() {
        MatcherAssert.assertThat(
            "runtime retention cannot be visible to reflection",
            new AnnotationRetention(Retentions.RuntimeLevel.class).visible(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("SOURCE до байткода не доживает")
    void dropsSourceRetention() {
        MatcherAssert.assertThat(
            "source retention cannot be invisible at runtime",
            new AnnotationRetention(Retentions.SourceLevel.class).visible(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("из четырёх аннотаций на классе в runtime видна ровно одна")
    void keepsOnlyRuntimeAnnotation() {
        MatcherAssert.assertThat(
            "only the runtime annotation cannot survive to reflection",
            new VisibleAnnotations(TripleAnnotated.class).names(),
            Matchers.contains("RuntimeLevel")
        );
    }

    @Test
    @DisplayName("то же правило действует и на полях")
    void appliesRetentionToFields() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "retention rule cannot apply to fields as well",
            new VisibleAnnotations(TripleAnnotated.class.getDeclaredField("field")).names(),
            Matchers.contains("RuntimeLevel")
        );
    }

    @Test
    @DisplayName("маркерная аннотация значений не несёт — важен факт присутствия")
    void treatsMarkerAsFlag() {
        MatcherAssert.assertThat(
            "marker annotation cannot work as a bare flag",
            Annotated.class.isAnnotationPresent(Marker.class),
            Matchers.equalTo(true)
        );
    }
}

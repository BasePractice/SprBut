/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import java.lang.annotation.Retention;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 36: Executable, Parameter, AnnotatedElement, Array.
 * @since 1.0
 */
@DisplayName("Слайд 36: Executable, Parameter, AnnotatedElement, Array")
final class ParametersTest {

    @Test
    @DisplayName("конструктор и метод разбираются одним кодом — оба Executable")
    void treatsConstructorAndMethodAlike() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "constructor and method cannot share the same parameter code",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .descriptions(),
            Matchers.contains("long millis", "String label")
        );
    }

    @Test
    @DisplayName("имена параметров сохранены благодаря флагу -parameters")
    void keepsParameterNames() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "parameter names cannot survive compilation",
            new Parameters(Service.class.getMethod("configure", long.class, String.class)).named(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("точки внедрения находятся по аннотации на параметре")
    void findsInjectionPoints() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "injection points cannot be found by the parameter annotation",
            new Parameters(
                Service.class.getConstructor(String.class, int.class, boolean.class)
            ).injectionPoints(),
            Matchers.contains("appName", "retries")
        );
    }

    @Test
    @DisplayName("имя из аннотации служит квалификатором")
    void usesAnnotationValueAsQualifier() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "annotation value cannot serve as the qualifier",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .injectionPoints(),
            Matchers.contains("timeout")
        );
    }

    @Test
    @DisplayName("аннотации читаются с любого элемента одинаково")
    void readsAnnotationsFromAnyElement() {
        MatcherAssert.assertThat(
            "annotations cannot be read from any element the same way",
            new ElementAnnotations(Injected.class).names(),
            Matchers.hasItem("Retention")
        );
    }

    @Test
    @DisplayName("аннотация на аннотации — тоже обычный AnnotatedElement")
    void readsMetaAnnotation() {
        MatcherAssert.assertThat(
            "meta annotation cannot be read like any other",
            new ElementAnnotations(Retention.class).names(),
            Matchers.hasItem("Documented")
        );
    }

    @Test
    @DisplayName("массив с типом элемента из runtime создаётся фабрикой")
    void createsArrayReflectively() {
        MatcherAssert.assertThat(
            "runtime typed array cannot be created",
            new ArrayValue(new ReflectiveArray(String.class).single(4)).length(),
            Matchers.equalTo(4)
        );
    }

    @Test
    @DisplayName("многомерный массив создаётся тем же API")
    void createsMatrix() {
        MatcherAssert.assertThat(
            "matrix cannot be created by the same API",
            ((int[][]) new ReflectiveArray(int.class).matrix(2, 3))[1].length,
            Matchers.equalTo(3)
        );
    }

    @Test
    @DisplayName("элемент примитивного массива читается без приведения к Object[]")
    void readsPrimitiveElement() {
        final Object array = new ReflectiveArray(int.class).single(2);
        final ArrayValue value = new ArrayValue(array);
        value.assign(0, 7);
        MatcherAssert.assertThat(
            "primitive array element cannot be read without a cast",
            value.element(0),
            Matchers.equalTo(7)
        );
    }

    @Test
    @DisplayName("синтетических параметров у обычного метода нет")
    void reportsNoSyntheticParameters() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "plain method cannot report an empty synthetic list",
            new Parameters(Service.class.getMethod("configure", long.class, String.class))
                .synthetic(),
            Matchers.equalTo(List.of())
        );
    }
}

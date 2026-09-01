/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m06.samples.OrderApi;
import ru.sprbut.m06.web.Controller;

/**
 * Расширенный пример: сканер слитых аннотаций.
 * @since 1.0
 */
@DisplayName("Расширенный пример: сканер слитых аннотаций")
final class MergedAnnotationTest {

    @Test
    @DisplayName("прямая аннотация находится и читается как есть")
    void readsDirectAnnotation() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "direct annotation cannot be read as is",
            new MergedAnnotation<>(UserController.class.getMethod("raw"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            Matchers.equalTo("/raw")
        );
    }

    @Test
    @DisplayName("@AliasFor переносит значение в элемент с другим именем")
    void appliesAliasFor() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "alias cannot carry the value into a differently named element",
            new MergedAnnotation<>(UserController.class.getMethod("list"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            Matchers.equalTo("/users")
        );
    }

    @Test
    @DisplayName("значение мета-аннотации доходит до результата")
    void keepsMetaAnnotationValue() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "meta annotation value cannot reach the result",
            new MergedAnnotation<>(UserController.class.getMethod("list"), RequestMapping.class)
                .find().orElseThrow().value("method"),
            Matchers.equalTo(HttpMethod.GET)
        );
    }

    @Test
    @DisplayName("одноимённый элемент переопределяет мета-аннотацию без алиаса")
    void overridesByMatchingName() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "matching name cannot override the meta annotation",
            (String[]) new MergedAnnotation<>(
                UserController.class.getMethod("listActive"), RequestMapping.class
            ).find().orElseThrow().value("produces"),
            Matchers.arrayContaining("application/json", "application/xml")
        );
    }

    @Test
    @DisplayName("значение из композиции второго уровня доходит вниз")
    void carriesValueFromSecondLevel() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "second level value cannot reach the target annotation",
            (String[]) new MergedAnnotation<>(
                UserController.class.getMethod("json"), RequestMapping.class
            ).find().orElseThrow().value("produces"),
            Matchers.arrayContaining("application/json")
        );
    }

    @Test
    @DisplayName("цепочка второго уровня проходится до конца")
    void followsTwoStepChain() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "two step chain cannot be followed to the end",
            new MergedAnnotation<>(UserController.class.getMethod("json"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            Matchers.equalTo("/users/json")
        );
    }

    @Test
    @DisplayName("путь мета-аннотаций сохраняется целиком")
    void keepsMetaPath() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "meta path cannot be kept whole",
            new MergedAnnotation<>(UserController.class.getMethod("json"), RequestMapping.class)
                .find().orElseThrow().path(),
            Matchers.hasSize(3)
        );
    }

    @Test
    @DisplayName("метод без аннотаций ничего не даёт")
    void findsNothingOnPlainMethod() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "plain method cannot yield an empty result",
            new MergedAnnotation<>(UserController.class.getMethod("plain"), RequestMapping.class)
                .find().isEmpty(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("алиас на несуществующий элемент отбивается сразу")
    void rejectsBrokenAlias() throws NoSuchMethodException {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new MergedAnnotation<>(
                UserController.class.getMethod("broken"), RequestMapping.class
            ).find()
        );
    }

    @Test
    @DisplayName("подъём по иерархии находит то, чего не находит @Inherited")
    void climbsHierarchy() {
        MatcherAssert.assertThat(
            "hierarchy search cannot find the composed annotation",
            new HierarchyMerged<>(OrderApi.class, Controller.class).find().isPresent(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("плоский список показывает всё, что навешано, включая мета-аннотации")
    void flattensEverything() {
        MatcherAssert.assertThat(
            "flat list cannot include the meta annotations",
            new Flattened(OrderApi.class).names(),
            Matchers.hasItem("@Controller")
        );
    }
}

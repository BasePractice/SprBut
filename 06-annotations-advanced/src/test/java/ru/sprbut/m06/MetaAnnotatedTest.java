/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m06.samples.NotAController;
import ru.sprbut.m06.samples.OrderApi;
import ru.sprbut.m06.samples.PlainController;
import ru.sprbut.m06.samples.UserApi;
import ru.sprbut.m06.web.Controller;
import ru.sprbut.m06.web.ResponseBody;
import ru.sprbut.m06.web.RestController;

/**
 * Слайд 55: @RestController = @Controller + @ResponseBody.
 * @since 1.0
 */
@DisplayName("Слайд 55: @RestController = @Controller + @ResponseBody")
final class MetaAnnotatedTest {

    @Test
    @DisplayName("язык мета-аннотации не раскрывает — наивная проверка не находит ничего")
    void dontSeeMetaAnnotationDirectly() {
        MatcherAssert.assertThat(
            "language cannot resolve the meta annotation by itself",
            new MetaAnnotated(UserApi.class).direct(Controller.class),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("рекурсивный поиск находит мета-аннотацию первого уровня")
    void findsFirstLevelMetaAnnotation() {
        MatcherAssert.assertThat(
            "recursive search cannot find the first level meta annotation",
            new MetaAnnotated(UserApi.class).deep(Controller.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("и второго уровня тоже — цепочки бывают длиннее одного шага")
    void findsSecondLevelMetaAnnotation() {
        MatcherAssert.assertThat(
            "recursive search cannot follow a two step chain",
            new MetaAnnotated(OrderApi.class).deep(Controller.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("прямая аннотация находится обоими способами")
    void findsDirectAnnotation() {
        MatcherAssert.assertThat(
            "direct annotation cannot be found the naive way",
            new MetaAnnotated(PlainController.class).direct(Controller.class),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("класс без аннотаций не находится никак")
    void dontFindOnPlainClass() {
        MatcherAssert.assertThat(
            "unannotated class cannot stay unmatched",
            new MetaAnnotated(NotAController.class).deep(Controller.class),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("цепочка печатается целиком, с отступами по глубине")
    void printsWholeChain() {
        MatcherAssert.assertThat(
            "annotation chain cannot show the nested level",
            new MetaAnnotated(UserApi.class).chain(),
            Matchers.hasItem(String.format("  @%s", Controller.class.getSimpleName()))
        );
    }

    @Test
    @DisplayName("вторая мета-аннотация композиции тоже попадает в цепочку")
    void printsBothMetaAnnotations() {
        MatcherAssert.assertThat(
            "second meta annotation cannot appear in the chain",
            new MetaAnnotated(UserApi.class).chain(),
            Matchers.hasItem(String.format("  @%s", ResponseBody.class.getSimpleName()))
        );
    }

    @Test
    @DisplayName("служебные аннотации языка в отчёт не попадают")
    void hidesLanguageAnnotations() {
        MatcherAssert.assertThat(
            "language annotations cannot be filtered out of the report",
            new MetaAnnotated(UserApi.class).chain(),
            Matchers.not(Matchers.hasItem("  @Retention"))
        );
    }

    @Test
    @DisplayName("сама композитная аннотация в цепочке первая")
    void startsChainWithComposite() {
        MatcherAssert.assertThat(
            "chain cannot start with the composite annotation",
            new MetaAnnotated(UserApi.class).chain().get(0),
            Matchers.equalTo(String.format("@%s", RestController.class.getSimpleName()))
        );
    }
}

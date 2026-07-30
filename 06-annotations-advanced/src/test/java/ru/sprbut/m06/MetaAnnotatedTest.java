package ru.sprbut.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m06.samples.NotAController;
import ru.sprbut.m06.samples.OrderApi;
import ru.sprbut.m06.samples.PlainController;
import ru.sprbut.m06.samples.UserApi;
import ru.sprbut.m06.web.Controller;
import ru.sprbut.m06.web.ResponseBody;
import ru.sprbut.m06.web.RestController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайд 55: @RestController = @Controller + @ResponseBody")
final class MetaAnnotatedTest {

    @Test
    @DisplayName("язык мета-аннотации не раскрывает — наивная проверка не находит ничего")
    void dontSeeMetaAnnotationDirectly() {
        assertThat(
            "language cannot resolve the meta annotation by itself",
            new MetaAnnotated(UserApi.class).direct(Controller.class),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("рекурсивный поиск находит мета-аннотацию первого уровня")
    void findsFirstLevelMetaAnnotation() {
        assertThat(
            "recursive search cannot find the first level meta annotation",
            new MetaAnnotated(UserApi.class).deep(Controller.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("и второго уровня тоже — цепочки бывают длиннее одного шага")
    void findsSecondLevelMetaAnnotation() {
        assertThat(
            "recursive search cannot follow a two step chain",
            new MetaAnnotated(OrderApi.class).deep(Controller.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("прямая аннотация находится обоими способами")
    void findsDirectAnnotation() {
        assertThat(
            "direct annotation cannot be found the naive way",
            new MetaAnnotated(PlainController.class).direct(Controller.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("класс без аннотаций не находится никак")
    void dontFindOnPlainClass() {
        assertThat(
            "unannotated class cannot stay unmatched",
            new MetaAnnotated(NotAController.class).deep(Controller.class),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("цепочка печатается целиком, с отступами по глубине")
    void printsWholeChain() {
        assertThat(
            "annotation chain cannot show the nested level",
            new MetaAnnotated(UserApi.class).chain(),
            hasItem("  @" + Controller.class.getSimpleName())
        );
    }

    @Test
    @DisplayName("вторая мета-аннотация композиции тоже попадает в цепочку")
    void printsBothMetaAnnotations() {
        assertThat(
            "second meta annotation cannot appear in the chain",
            new MetaAnnotated(UserApi.class).chain(),
            hasItem("  @" + ResponseBody.class.getSimpleName())
        );
    }

    @Test
    @DisplayName("служебные аннотации языка в отчёт не попадают")
    void hidesLanguageAnnotations() {
        assertThat(
            "language annotations cannot be filtered out of the report",
            new MetaAnnotated(UserApi.class).chain(),
            not(hasItem("  @Retention"))
        );
    }

    @Test
    @DisplayName("сама композитная аннотация в цепочке первая")
    void startsChainWithComposite() {
        assertThat(
            "chain cannot start with the composite annotation",
            new MetaAnnotated(UserApi.class).chain().get(0),
            equalTo("@" + RestController.class.getSimpleName())
        );
    }
}

package ru.sprbut.m06.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m06.samples.OrderApi;
import ru.sprbut.m06.web.Controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: сканер слитых аннотаций")
final class MergedAnnotationTest {

    @Test
    @DisplayName("прямая аннотация находится и читается как есть")
    void readsDirectAnnotation() throws NoSuchMethodException {
        assertThat(
            "direct annotation cannot be read as is",
            new MergedAnnotation<>(UserController.class.getMethod("raw"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            equalTo("/raw")
        );
    }

    @Test
    @DisplayName("@AliasFor переносит значение в элемент с другим именем")
    void appliesAliasFor() throws NoSuchMethodException {
        assertThat(
            "alias cannot carry the value into a differently named element",
            new MergedAnnotation<>(UserController.class.getMethod("list"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            equalTo("/users")
        );
    }

    @Test
    @DisplayName("значение мета-аннотации доходит до результата")
    void keepsMetaAnnotationValue() throws NoSuchMethodException {
        assertThat(
            "meta annotation value cannot reach the result",
            new MergedAnnotation<>(UserController.class.getMethod("list"), RequestMapping.class)
                .find().orElseThrow().value("method"),
            equalTo(HttpMethod.GET)
        );
    }

    @Test
    @DisplayName("одноимённый элемент переопределяет мета-аннотацию без алиаса")
    void overridesByMatchingName() throws NoSuchMethodException {
        assertThat(
            "matching name cannot override the meta annotation",
            (String[]) new MergedAnnotation<>(
                UserController.class.getMethod("listActive"), RequestMapping.class
            ).find().orElseThrow().value("produces"),
            arrayContaining("application/json", "application/xml")
        );
    }

    @Test
    @DisplayName("значение из композиции второго уровня доходит вниз")
    void carriesValueFromSecondLevel() throws NoSuchMethodException {
        assertThat(
            "second level value cannot reach the target annotation",
            (String[]) new MergedAnnotation<>(
                UserController.class.getMethod("json"), RequestMapping.class
            ).find().orElseThrow().value("produces"),
            arrayContaining("application/json")
        );
    }

    @Test
    @DisplayName("цепочка второго уровня проходится до конца")
    void followsTwoStepChain() throws NoSuchMethodException {
        assertThat(
            "two step chain cannot be followed to the end",
            new MergedAnnotation<>(UserController.class.getMethod("json"), RequestMapping.class)
                .find().orElseThrow().text("path"),
            equalTo("/users/json")
        );
    }

    @Test
    @DisplayName("путь мета-аннотаций сохраняется целиком")
    void keepsMetaPath() throws NoSuchMethodException {
        assertThat(
            "meta path cannot be kept whole",
            new MergedAnnotation<>(UserController.class.getMethod("json"), RequestMapping.class)
                .find().orElseThrow().path(),
            hasSize(3)
        );
    }

    @Test
    @DisplayName("метод без аннотаций ничего не даёт")
    void findsNothingOnPlainMethod() throws NoSuchMethodException {
        assertThat(
            "plain method cannot yield an empty result",
            new MergedAnnotation<>(UserController.class.getMethod("plain"), RequestMapping.class)
                .find().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("алиас на несуществующий элемент отбивается сразу")
    void rejectsBrokenAlias() throws NoSuchMethodException {
        assertThrows(
            IllegalStateException.class,
            () -> new MergedAnnotation<>(
                UserController.class.getMethod("broken"), RequestMapping.class
            ).find()
        );
    }

    @Test
    @DisplayName("подъём по иерархии находит то, чего не находит @Inherited")
    void climbsHierarchy() {
        assertThat(
            "hierarchy search cannot find the composed annotation",
            new HierarchyMerged<>(OrderApi.class, Controller.class).find().isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("плоский список показывает всё, что навешано, включая мета-аннотации")
    void flattensEverything() {
        assertThat(
            "flat list cannot include the meta annotations",
            new Flattened(OrderApi.class).names(),
            hasItem("@Controller")
        );
    }
}

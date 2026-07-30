package ru.sprbut.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 55: @RestController = @Controller + @ResponseBody")
class CompositionTest {

    @Test
    @DisplayName("Композиция — это просто аннотации, навешанные на аннотацию")
    void compositionIsJustMetaAnnotations() {
        assertThat(Composition.RestController.class
                .isAnnotationPresent(Composition.Controller.class)).isTrue();
        assertThat(Composition.RestController.class
                .isAnnotationPresent(Composition.ResponseBody.class)).isTrue();
    }

    @Test
    @DisplayName("Язык мета-аннотации не раскрывает: getAnnotation вернёт null")
    void languageDoesNotUnwrapMetaAnnotations() {
        assertThat(Composition.directlyAnnotated(
                Composition.UserApi.class, Composition.RestController.class)).isTrue();
        assertThat(Composition.directlyAnnotated(
                Composition.UserApi.class, Composition.Controller.class))
                .as("@Controller навешан на @RestController, а не на класс")
                .isFalse();
    }

    @Test
    @DisplayName("Рекурсивный поиск находит то, что язык не раскрывает")
    void recursiveSearchFindsMetaAnnotations() {
        assertThat(Composition.metaAnnotated(
                Composition.UserApi.class, Composition.Controller.class)).isTrue();
        assertThat(Composition.metaAnnotated(
                Composition.UserApi.class, Composition.ResponseBody.class)).isTrue();
    }

    @Test
    @DisplayName("Цепочка мета-аннотаций может быть любой длины")
    void chainsCanBeDeep() {
        assertThat(Composition.metaAnnotated(
                Composition.OrderApi.class, Composition.Controller.class)).isTrue();
        assertThat(Composition.annotationChain(Composition.OrderApi.class))
                .containsExactly("@ApiController", "  @RestController", "    @Controller",
                        "    @ResponseBody");
    }

    @Test
    @DisplayName("Прямое использование тоже находится — поиск работает на нулевой глубине")
    void directUsageIsAlsoFound() {
        assertThat(Composition.metaAnnotated(
                Composition.PlainController.class, Composition.Controller.class)).isTrue();
    }

    @Test
    @DisplayName("Класс без аннотаций не даёт ложных срабатываний")
    void unannotatedClassFindsNothing() {
        assertThat(Composition.metaAnnotated(
                Composition.NotAController.class, Composition.Controller.class)).isFalse();
        assertThat(Composition.annotationChain(Composition.NotAController.class)).isEmpty();
    }

    @Test
    @DisplayName("Служебные аннотации языка исключаются — иначе обход зациклится")
    void javaBuiltinsAreExcluded() {
        assertThat(Composition.isJavaBuiltin(java.lang.annotation.Retention.class)).isTrue();
        assertThat(Composition.isJavaBuiltin(Composition.Controller.class)).isFalse();

        // @Retention сама помечена @Retention — без фильтра это бесконечный цикл
        assertThat(java.lang.annotation.Retention.class
                .isAnnotationPresent(java.lang.annotation.Retention.class)).isTrue();
    }
}

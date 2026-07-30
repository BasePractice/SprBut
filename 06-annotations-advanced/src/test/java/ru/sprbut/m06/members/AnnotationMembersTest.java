package ru.sprbut.m06.members;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

@DisplayName("Слайды 50–52: типы элементов и значения по умолчанию")
final class AnnotationMembersTest {

    @Test
    @DisplayName("обязателен только элемент без default")
    void listsRequiredElements() {
        assertThat(
            "element without default cannot be the only required one",
            new AnnotationMembers(Operation.class).required(),
            contains("name")
        );
    }

    @Test
    @DisplayName("значение по умолчанию примитива читается отдельно от использования")
    void readsPrimitiveDefault() {
        assertThat(
            "primitive default cannot be read",
            new AnnotationMembers(Operation.class).defaults(),
            hasEntry("timeout", 30)
        );
    }

    @Test
    @DisplayName("значение по умолчанию типа Class печатается коротким именем")
    void readsClassDefault() {
        assertThat(
            "class default cannot be printed readably",
            new AnnotationMembers(Operation.class).defaults(),
            hasEntry("rollbackFor", "RuntimeException")
        );
    }

    @Test
    @DisplayName("вложенная аннотация по умолчанию тоже читается")
    void readsNestedAnnotationDefault() {
        assertThat(
            "nested annotation default cannot be read",
            new AnnotationMembers(Operation.class).defaults(),
            hasEntry("retry", "@Retry")
        );
    }

    @Test
    @DisplayName("массив по умолчанию печатается содержимым, а не хэшем")
    void readsArrayDefault() {
        assertThat(
            "array default cannot be printed by its contents",
            new AnnotationMembers(Operation.class).defaults(),
            hasEntry("allowed", "[DEFAULT]")
        );
    }

    @Test
    @DisplayName("минимальное использование берёт значения из умолчаний")
    void fallsBackToDefaults() throws NoSuchMethodException {
        assertThat(
            "minimal usage cannot fall back to the defaults",
            new AnnotationValues(
                Service.class.getMethod("withDefaults").getAnnotation(Operation.class)
            ).values(),
            hasEntry("timeout", 30)
        );
    }

    @Test
    @DisplayName("заданное значение перебивает умолчание")
    void keepsExplicitValue() throws NoSuchMethodException {
        assertThat(
            "explicit value cannot override the default",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            hasEntry("timeout", 5)
        );
    }

    @Test
    @DisplayName("enum читается своей константой")
    void readsEnumValue() throws NoSuchMethodException {
        assertThat(
            "enum element cannot be read as its constant",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            hasEntry("isolation", Isolation.SERIALIZABLE)
        );
    }

    @Test
    @DisplayName("массив строк читается содержимым")
    void readsStringArray() throws NoSuchMethodException {
        assertThat(
            "string array cannot be read by its contents",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values(),
            hasEntry("tags", "[critical, billing]")
        );
    }

    @Test
    @DisplayName("вложенная аннотация со своими значениями остаётся аннотацией")
    void readsNestedAnnotationValue() throws NoSuchMethodException {
        assertThat(
            "nested annotation value cannot stay an annotation",
            new AnnotationValues(
                Service.class.getMethod("withEverything").getAnnotation(Operation.class)
            ).values().get("retry"),
            equalTo("@Retry")
        );
    }
}

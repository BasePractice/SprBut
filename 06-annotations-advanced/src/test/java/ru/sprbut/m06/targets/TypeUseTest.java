package ru.sprbut.m06.targets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайды 48–49: цели, появившиеся после Java 8")
final class TypeUseTest {

    @Test
    @DisplayName("TYPE_USE читается с AnnotatedType, а не с поля")
    void readsAnnotationOnFieldType() throws NoSuchFieldException {
        assertThat(
            "type use annotation cannot be read from the annotated type",
            new TypeUse(Holder.class.getField("direct")).onType(),
            contains("NonNull")
        );
    }

    @Test
    @DisplayName("обычный getAnnotations() этих аннотаций не видит вовсе")
    void dontSeeTypeUseOnField() throws NoSuchFieldException {
        assertThat(
            "plain field lookup cannot miss the type use annotation",
            Holder.class.getField("direct").getAnnotations().length,
            equalTo(0)
        );
    }

    @Test
    @DisplayName("аннотация внутри дженерика лежит на аргументе типа")
    void readsAnnotationInsideGenerics() throws NoSuchFieldException {
        assertThat(
            "annotation inside generics cannot be read from the type argument",
            new TypeUse(Holder.class.getField("insideGenerics")).onArguments(),
            contains("NonNull")
        );
    }

    @Test
    @DisplayName("у непомеченного поля аннотаций типа нет")
    void reportsNoAnnotationsForPlainField() throws NoSuchFieldException {
        assertThat(
            "plain field cannot report an empty list",
            new TypeUse(Holder.class.getField("plain")).onArguments(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("TYPE_PARAMETER читается с объявления переменной типа")
    void readsTypeParameterAnnotation() {
        assertThat(
            "type parameter annotation cannot be read",
            new TypeParameters(Holder.class).names(0),
            contains("Comparablish")
        );
    }

    @Test
    @DisplayName("RECORD_COMPONENT — отдельная ветка API")
    void readsRecordComponentAnnotation() {
        assertThat(
            "record component annotation cannot be read",
            new RecordColumn(UserRow.class, "id").name().orElseThrow(),
            equalTo("user_id")
        );
    }

    @Test
    @DisplayName("несуществующий компонент record — понятная ошибка")
    void failsOnUnknownComponent() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RecordColumn(UserRow.class, "nope").name()
        );
    }

    @Test
    @DisplayName("ANNOTATION_TYPE: мета-аннотация читается с аннотации как с класса")
    void readsMetaAnnotationOnAnnotation() {
        assertThat(
            "meta annotation cannot be read from an annotation type",
            new Layer(WebLayer.class).name().orElseThrow(),
            equalTo("web")
        );
    }

    @Test
    @DisplayName("аннотация без стереотипа слоя не называет")
    void reportsNoLayerWithoutStereotype() {
        assertThat(
            "annotation without stereotype cannot report an empty layer",
            new Layer(NonNull.class).name().isEmpty(),
            equalTo(true)
        );
    }
}

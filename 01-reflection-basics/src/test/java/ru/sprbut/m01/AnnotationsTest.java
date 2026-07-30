package ru.sprbut.m01;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.extended.JsonIgnore;
import ru.sprbut.m01.extended.JsonProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд 9: чтение аннотаций через рефлексию")
final class AnnotationsTest {

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    private @interface SourceOnly {
    }

    @SuppressWarnings("unused")
    private static final class Sample {

        @JsonProperty("account_id")
        @JsonIgnore
        String id;

        String plain;

        @SourceOnly
        String invisible;
    }

    @Test
    @DisplayName("isAnnotationPresent проверяет наличие аннотации")
    void detectsPresentAnnotation() {
        assertThat(
            "present annotation cannot be detected",
            new Annotations(new Declared(Sample.class).field("id")).has(JsonProperty.class),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("getAnnotation отдаёт саму аннотацию со значениями элементов")
    void readsAnnotationValue() {
        assertThat(
            "annotation element value cannot be read",
            new Annotations(new Declared(Sample.class).field("id"))
                .find(JsonProperty.class)
                .map(JsonProperty::value)
                .orElseThrow(),
            equalTo("account_id")
        );
    }

    @Test
    @DisplayName("поле без аннотации даёт пустой Optional, а не исключение")
    void dontFailOnMissingAnnotation() {
        assertThat(
            "missing annotation cannot yield an empty optional",
            new Annotations(new Declared(Sample.class).field("plain")).find(JsonProperty.class).isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("все runtime-аннотации элемента перечисляются разом")
    void listsRuntimeAnnotations() {
        assertThat(
            "runtime annotations cannot be listed together",
            new Annotations(new Declared(Sample.class).field("id")).names(),
            contains("JsonIgnore", "JsonProperty")
        );
    }

    @Test
    @DisplayName("аннотация с RetentionPolicy.SOURCE в runtime не существует вовсе")
    void dontSeeSourceRetention() {
        assertThat(
            "source retained annotation cannot stay invisible at runtime",
            new Annotations(new Declared(Sample.class).field("invisible")).names(),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("аннотации читаются с любого AnnotatedElement, в том числе с самой аннотации")
    void readsFromAnyAnnotatedElement() {
        assertThat(
            "annotation on an annotation cannot be read the same way",
            new Annotations(JsonProperty.class).has(Retention.class),
            equalTo(true)
        );
    }
}

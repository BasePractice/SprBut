package ru.sprbut.m01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.extended.JsonIgnore;
import ru.sprbut.m01.extended.JsonProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 9: чтение аннотаций через рефлексию")
class AnnotationReaderTest {

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    private @interface SourceOnly {
    }

    @SuppressWarnings("unused")
    private static class Sample {
        @JsonProperty("account_id")
        @JsonIgnore
        String id;

        @SourceOnly
        String invisible;

        String plain;
    }

    @Test
    @DisplayName("isAnnotationPresent проверяет наличие, getAnnotation отдаёт значения")
    void readsAnnotation() throws NoSuchFieldException {
        Field id = Sample.class.getDeclaredField("id");

        assertThat(AnnotationReader.isPresent(id, JsonProperty.class)).isTrue();
        assertThat(AnnotationReader.find(id, JsonProperty.class))
                .get()
                .extracting(JsonProperty::value)
                .isEqualTo("account_id");
    }

    @Test
    @DisplayName("Поле без аннотации — пустой Optional, а не исключение")
    void missingAnnotationIsEmpty() throws NoSuchFieldException {
        Field plain = Sample.class.getDeclaredField("plain");

        assertThat(AnnotationReader.isPresent(plain, JsonProperty.class)).isFalse();
        assertThat(AnnotationReader.find(plain, JsonProperty.class)).isEmpty();
        assertThat(AnnotationReader.names(plain)).isEmpty();
    }

    @Test
    @DisplayName("Все RUNTIME-аннотации элемента перечисляются разом")
    void listsAllRuntimeAnnotations() throws NoSuchFieldException {
        Field id = Sample.class.getDeclaredField("id");

        assertThat(AnnotationReader.names(id)).containsExactly("JsonIgnore", "JsonProperty");
    }

    @Test
    @DisplayName("Аннотация с RetentionPolicy.SOURCE в runtime не видна вообще")
    void sourceRetentionIsInvisibleAtRuntime() throws NoSuchFieldException {
        Field invisible = Sample.class.getDeclaredField("invisible");

        assertThat(AnnotationReader.names(invisible)).isEmpty();
    }

    @Test
    @DisplayName("Аннотации читаются с любого AnnotatedElement — класса, поля, метода")
    void worksForAnyAnnotatedElement() {
        assertThat(AnnotationReader.isPresent(JsonProperty.class, Retention.class)).isTrue();
        assertThat(AnnotationReader.find(JsonProperty.class, Retention.class))
                .get()
                .extracting(Retention::value)
                .isEqualTo(RetentionPolicy.RUNTIME);
    }
}

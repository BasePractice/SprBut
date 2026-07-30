package ru.sprbut.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 48–49: ANNOTATION_TYPE, TYPE_USE, TYPE_PARAMETER, RECORD_COMPONENT")
class ModernTargetsTest {

    private Field field(String name) throws NoSuchFieldException {
        return ModernTargets.Holder.class.getField(name);
    }

    @Test
    @DisplayName("TYPE_USE: аннотация на типе поля читается через AnnotatedType")
    void readsAnnotationOnFieldType() throws NoSuchFieldException {
        assertThat(ModernTargets.annotationsOnFieldType(field("direct")))
                .containsExactly("NonNull");
        assertThat(ModernTargets.annotationsOnFieldType(field("plain"))).isEmpty();
    }

    @Test
    @DisplayName("TYPE_USE достаёт аннотацию изнутри дженерика — getAnnotations() её не видит")
    void readsAnnotationInsideGenerics() throws NoSuchFieldException {
        Field field = field("insideGenerics");

        assertThat(field.getAnnotations()).as("на самом поле аннотаций нет").isEmpty();
        assertThat(ModernTargets.annotationsOnTypeArguments(field)).containsExactly("NonNull");
    }

    @Test
    @DisplayName("TYPE_PARAMETER: аннотация на объявлении <T>")
    void readsAnnotationOnTypeParameter() {
        assertThat(ModernTargets.annotationsOnTypeParameter(ModernTargets.Holder.class, 0))
                .containsExactly("Comparablish");
    }

    @Test
    @DisplayName("RECORD_COMPONENT: у компонентов record своя ветка API")
    void readsRecordComponentAnnotations() {
        assertThat(ModernTargets.columnNameOf(ModernTargets.UserRow.class, "id"))
                .isEqualTo("user_id");
        assertThat(ModernTargets.columnNameOf(ModernTargets.UserRow.class, "login"))
                .isEqualTo("login");
    }

    @Test
    @DisplayName("ANNOTATION_TYPE: аннотация на аннотации читается как с обычного класса")
    void readsMetaAnnotation() {
        assertThat(ModernTargets.layerOf(ModernTargets.Controller.class)).isEqualTo("web");
        assertThat(ModernTargets.layerOf(ModernTargets.NonNull.class)).isNull();
    }

    @Test
    @DisplayName("@Documented влияет только на javadoc, но в runtime видна как обычная мета-аннотация")
    void documentedIsJustAnotherMetaAnnotation() {
        assertThat(ModernTargets.Column.class.isAnnotationPresent(java.lang.annotation.Documented.class))
                .isTrue();
    }
}

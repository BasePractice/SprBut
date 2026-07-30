package ru.sprbut.m05;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;
import ru.sprbut.m05.declarations.Retentions;
import ru.sprbut.m05.samples.Annotated;
import ru.sprbut.m05.samples.TripleAnnotated;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайды 39–40: @Target и @Retention")
final class AnnotationTargetsTest {

    @Test
    @DisplayName("@Target перечисляет разрешённые места")
    void listsAllowedTargets() {
        assertThat(
            "allowed targets cannot be read from the meta annotation",
            new AnnotationTargets(Level.class).allowed(),
            hasItems(ElementType.TYPE, ElementType.FIELD, ElementType.METHOD)
        );
    }

    @Test
    @DisplayName("аннотация, разрешённая только на типах, поля не допускает")
    void restrictsToTypes() {
        assertThat(
            "type only annotation cannot be rejected for fields",
            new AnnotationTargets(Audited.class).fields(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("@Target — ограничение компилятора: в runtime аннотация есть там, где её разрешили поставить")
    void appliesAtCompileTime() {
        assertThat(
            "annotation cannot appear where the target allows it",
            new VisibleAnnotations(Annotated.class).names(),
            hasItems("Level", "Marker")
        );
    }

    @Test
    @DisplayName("аннотация локальной переменной в class-файл не попадает вовсе")
    void dontKeepLocalVariableAnnotations() throws NoSuchMethodException {
        assertThat(
            "local variable annotation cannot vanish from the class file",
            new VisibleAnnotations(Annotated.class.getMethod("method", String.class)).names(),
            not(hasItem("SuppressWarnings"))
        );
    }

    @Test
    @DisplayName("без @Retention политика по умолчанию — CLASS")
    void defaultsToClassRetention() {
        assertThat(
            "missing retention cannot default to CLASS",
            new AnnotationRetention(Retentions.DefaultRetention.class).policy(),
            equalTo(RetentionPolicy.CLASS)
        );
    }

    @Test
    @DisplayName("только RUNTIME видна рефлексии")
    void keepsRuntimeVisible() {
        assertThat(
            "runtime retention cannot be visible to reflection",
            new AnnotationRetention(Retentions.RuntimeLevel.class).visible(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("SOURCE до байткода не доживает")
    void dropsSourceRetention() {
        assertThat(
            "source retention cannot be invisible at runtime",
            new AnnotationRetention(Retentions.SourceLevel.class).visible(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("из четырёх аннотаций на классе в runtime видна ровно одна")
    void keepsOnlyRuntimeAnnotation() {
        assertThat(
            "only the runtime annotation cannot survive to reflection",
            new VisibleAnnotations(TripleAnnotated.class).names(),
            contains("RuntimeLevel")
        );
    }

    @Test
    @DisplayName("то же правило действует и на полях")
    void appliesRetentionToFields() throws NoSuchFieldException {
        assertThat(
            "retention rule cannot apply to fields as well",
            new VisibleAnnotations(TripleAnnotated.class.getDeclaredField("field")).names(),
            contains("RuntimeLevel")
        );
    }

    @Test
    @DisplayName("маркерная аннотация значений не несёт — важен факт присутствия")
    void treatsMarkerAsFlag() {
        assertThat(
            "marker annotation cannot work as a bare flag",
            Annotated.class.isAnnotationPresent(Marker.class),
            equalTo(true)
        );
    }
}

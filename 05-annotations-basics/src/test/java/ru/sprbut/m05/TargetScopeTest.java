package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Audited;
import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;

import java.lang.annotation.ElementType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 39: @Target — где аннотацию разрешено ставить")
class TargetScopeTest {

    @Test
    @DisplayName("Список допустимых мест читается из мета-аннотации самой аннотации")
    void readsAllowedTargets() {
        assertThat(TargetScope.allowedTargets(Marker.class))
                .containsExactly(ElementType.TYPE, ElementType.METHOD);
        assertThat(TargetScope.allowedTargets(Level.class))
                .containsExactly(ElementType.TYPE, ElementType.METHOD, ElementType.FIELD);
    }

    @Test
    @DisplayName("@Target — ограничение компилятора: в runtime проверять уже нечего")
    void targetIsACompileTimeConstraint() {
        assertThat(TargetScope.allowsFields(Level.class)).isTrue();
        assertThat(TargetScope.allowsFields(Marker.class)).isFalse();
        assertThat(TargetScope.allowsTypes(Audited.class)).isTrue();
    }

    @Test
    @DisplayName("Аннотация читается с класса, поля и метода одинаково")
    void readsFromEveryElementKind() throws Exception {
        assertThat(TargetScope.Annotated.class.getAnnotation(Level.class).value())
                .isEqualTo("класс");
        assertThat(TargetScope.Annotated.class.getDeclaredField("field")
                .getAnnotation(Level.class).value()).isEqualTo("поле");
        assertThat(TargetScope.Annotated.class
                .getDeclaredMethod("method", String.class)
                .getAnnotation(Level.class).value()).isEqualTo("метод");
    }

    @Test
    @DisplayName("Аннотация локальной переменной в class-файл не попадает вовсе")
    void localVariableAnnotationsAreUnreadable() throws Exception {
        // На методе видны только аннотации самого метода — локальные переменные
        // не имеют представления в рефлексии ни при какой политике хранения
        var method = TargetScope.Annotated.class.getDeclaredMethod("method", String.class);

        assertThat(method.getAnnotations()).hasSize(2);
        assertThat(method.getAnnotationsByType(Level.class)).hasSize(1);
    }

    @Test
    @DisplayName("Отсутствие @Target означает «почти везде», а не «нигде»")
    void missingTargetMeansAlmostEverywhere() {
        assertThat(TargetScope.allowedTargets(SuppressWarnings.class)).isNotEmpty();
        assertThat(TargetScope.allowedTargets(NoTarget.class)).isEmpty();
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface NoTarget {
    }
}

package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Retentions;

import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 40: @Retention{SOURCE, CLASS, RUNTIME}")
class RetentionVisibilityTest {

    @Test
    @DisplayName("Политика читается с самой аннотации")
    void readsPolicy() {
        assertThat(RetentionVisibility.policyOf(Retentions.SourceLevel.class))
                .isEqualTo(RetentionPolicy.SOURCE);
        assertThat(RetentionVisibility.policyOf(Retentions.ClassLevel.class))
                .isEqualTo(RetentionPolicy.CLASS);
        assertThat(RetentionVisibility.policyOf(Retentions.RuntimeLevel.class))
                .isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    @DisplayName("Без @Retention действует политика по умолчанию — CLASS, а не RUNTIME")
    void defaultPolicyIsClass() {
        assertThat(RetentionVisibility.policyOf(Retentions.DefaultRetention.class))
                .isEqualTo(RetentionPolicy.CLASS);
        assertThat(RetentionVisibility.visibleAtRuntime(Retentions.DefaultRetention.class)).isFalse();
    }

    @Test
    @DisplayName("Из четырёх аннотаций на классе в runtime видна ровно одна")
    void onlyRuntimeAnnotationsAreVisibleOnClass() {
        assertThat(RetentionVisibility.visibleAnnotations(RetentionVisibility.TripleAnnotated.class))
                .containsExactly("RuntimeLevel");
    }

    @Test
    @DisplayName("То же правило действует для полей и методов")
    void sameRuleForFieldsAndMethods() throws Exception {
        assertThat(RetentionVisibility.visibleAnnotations(
                RetentionVisibility.TripleAnnotated.class.getDeclaredField("field")))
                .containsExactly("RuntimeLevel");
        assertThat(RetentionVisibility.visibleAnnotations(
                RetentionVisibility.TripleAnnotated.class.getDeclaredMethod("method")))
                .containsExactly("RuntimeLevel");
    }

    @Test
    @DisplayName("isAnnotationPresent для SOURCE- и CLASS-аннотаций всегда false")
    void nonRuntimeAnnotationsAreInvisible() {
        Class<?> type = RetentionVisibility.TripleAnnotated.class;

        assertThat(type.isAnnotationPresent(Retentions.SourceLevel.class)).isFalse();
        assertThat(type.isAnnotationPresent(Retentions.ClassLevel.class)).isFalse();
        assertThat(type.isAnnotationPresent(Retentions.RuntimeLevel.class)).isTrue();
    }

    @Test
    @DisplayName("Все аннотации Spring помечены RUNTIME — иначе контейнер их не увидит")
    void springAnnotationsAreRuntime() {
        assertThat(RetentionVisibility.visibleAtRuntime(Deprecated.class)).isTrue();
        assertThat(RetentionVisibility.visibleAtRuntime(Override.class)).isFalse();
    }
}

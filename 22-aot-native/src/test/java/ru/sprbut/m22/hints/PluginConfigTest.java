package ru.sprbut.m22.hints;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportRuntimeHints;
import ru.sprbut.m22.reflection.Plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд «AOT»: конфигурация объявляет свою рефлексию через @ImportRuntimeHints")
final class PluginConfigTest {

    @Test
    @DisplayName("бин собирается рефлексией и попадает в контекст")
    void buildsPluginReflectively() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(PluginConfig.class)) {
            assertThat(
                "reflectively built plugin cannot reach the context",
                context.getBean(Plugin.class).name(),
                equalTo("csv")
            );
        }
    }

    @Test
    @DisplayName("регистратор подсказок объявлен на самой конфигурации")
    void declaresHintsRegistrar() {
        assertThat(
            "configuration cannot point at its own hints registrar",
            PluginConfig.class.getAnnotation(ImportRuntimeHints.class).value(),
            arrayContaining(equalTo(PluginHints.class))
        );
    }
}

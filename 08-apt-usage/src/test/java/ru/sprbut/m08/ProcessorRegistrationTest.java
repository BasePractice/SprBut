package ru.sprbut.m08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 66: регистрация процессора через META-INF/services")
class ProcessorRegistrationTest {

    @Test
    @DisplayName("Файл регистрации перечисляет все три процессора модуля 07")
    void serviceFileListsProcessors() {
        assertThat(ProcessorRegistration.declaredProcessorNames())
                .containsExactly(
                        "ru.sprbut.m07.BuilderProcessor",
                        "ru.sprbut.m07.TodoProcessor",
                        "ru.sprbut.m07.extended.RegistryProcessor");
    }

    @Test
    @DisplayName("ServiceLoader находит ровно то, что перечислено в файле")
    void serviceLoaderMatchesTheFile() {
        assertThat(ProcessorRegistration.loadedProcessorNames())
                .containsExactlyElementsOf(ProcessorRegistration.declaredProcessorNames());
    }

    @Test
    @DisplayName("Каждый процессор объявляет, какие аннотации он обрабатывает")
    void processorsDeclareSupportedAnnotations() {
        assertThat(ProcessorRegistration.supportedAnnotationsOf("ru.sprbut.m07.BuilderProcessor"))
                .containsExactly("ru.sprbut.m07.api.GenerateBuilder");
        assertThat(ProcessorRegistration.supportedAnnotationsOf("ru.sprbut.m07.TodoProcessor"))
                .containsExactly("ru.sprbut.m07.api.Todo");
        assertThat(ProcessorRegistration.supportedAnnotationsOf(
                "ru.sprbut.m07.extended.RegistryProcessor"))
                .containsExactly("ru.sprbut.m07.api.Registered");
    }

    @Test
    @DisplayName("По этому списку javac и решает, звать ли процессор в раунде")
    void supportedAnnotationsDriveInvocation() {
        // В модуле 08 есть @GenerateBuilder, @Todo и @Registered — значит
        // при сборке отработали все три процессора. Доказательство —
        // сгенерированные классы и предупреждения TODO в логе сборки.
        assertThat(ru.sprbut.m08.model.CustomerBuilder.class).isNotNull();
        assertThat(ru.sprbut.m08.generated.ModuleRegistry.size()).isPositive();
    }

    @Test
    @DisplayName("Незарегистрированный процессор запросить нельзя")
    void unknownProcessorIsRejected() {
        assertThatThrownBy(() -> ProcessorRegistration.supportedAnnotationsOf("ru.sprbut.Nope"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не зарегистрирован");
    }
}

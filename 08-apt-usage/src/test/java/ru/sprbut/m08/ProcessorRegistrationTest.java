package ru.sprbut.m08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 66: регистрация процессора через META-INF/services")
final class ProcessorRegistrationTest {

    @Test
    @DisplayName("файл регистрации перечисляет процессоры модуля 07")
    void listsDeclaredProcessors() {
        assertThat(
            "service file cannot list the declared processor",
            new ProcessorRegistration().declared(),
            hasItem("ru.sprbut.m07.BuilderProcessor")
        );
    }

    @Test
    @DisplayName("ServiceLoader загружает ровно то, что объявлено в файле")
    void loadsExactlyWhatIsDeclared() {
        assertThat(
            "service loader cannot load exactly the declared processors",
            new ProcessorRegistration().loaded(),
            equalTo(new ProcessorRegistration().declared())
        );
    }

    @Test
    @DisplayName("процессор сам объявляет, какие аннотации обрабатывает")
    void readsSupportedAnnotations() {
        assertThat(
            "processor cannot declare its supported annotations",
            new ProcessorRegistration().supported("ru.sprbut.m07.BuilderProcessor"),
            hasItem(containsString("Builder"))
        );
    }

    @Test
    @DisplayName("незарегистрированный процессор — понятная ошибка с его именем")
    void failsOnUnregisteredProcessor() {
        assertThat(
            "unregistered processor cannot be reported by name",
            assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessorRegistration().supported("ru.sprbut.Nope")
            ).getMessage(),
            containsString("ru.sprbut.Nope")
        );
    }

    @Test
    @DisplayName("путь регистрации — часть контракта javac, а не выдумка проекта")
    void keepsStandardServicePath() {
        assertThat(
            "service path cannot follow the javac contract",
            new ProcessorRegistration().servicePath(),
            equalTo("META-INF/services/javax.annotation.processing.Processor")
        );
    }
}

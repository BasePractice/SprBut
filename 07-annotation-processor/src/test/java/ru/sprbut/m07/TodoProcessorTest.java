package ru.sprbut.m07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 60: процессор-анализатор, который ничего не генерирует")
class TodoProcessorTest {

    @TempDir
    Path workDir;

    private CompilationHarness.Result compile(String code) {
        return CompilationHarness.compile(workDir,
                List.of(new CompilationHarness.Source("demo.Service", code)), new TodoProcessor());
    }

    @Test
    @DisplayName("Непроходящий TODO становится предупреждением, сборка проходит")
    void nonBlockingTodoIsAWarning() {
        CompilationHarness.Result result = compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;

                    public void run() {
                    }
                }
                """);

        assertThat(result.success()).isTrue();
        assertThat(result.warnings())
                .anyMatch(m -> m.contains("вынести в конфигурацию") && m.contains("timeout"));
        assertThat(result.generatedSources()).as("анализатор ничего не генерирует").isEmpty();
    }

    @Test
    @DisplayName("blocking = true роняет сборку — так работают Error Prone и NullAway")
    void blockingTodoFailsTheBuild() {
        CompilationHarness.Result result = compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                @Todo(value = "нельзя выпускать в прод", blocking = true)
                public class Service {
                }
                """);

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(m -> m.contains("нельзя выпускать в прод"));
    }

    @Test
    @DisplayName("Диагностика ставится на конкретный элемент — с номером строки")
    void diagnosticPointsAtTheElement() {
        CompilationHarness.Result result = compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("починить")
                    public void broken() {
                    }
                }
                """);

        assertThat(result.diagnostics())
                .filteredOn(d -> d.getKind() == javax.tools.Diagnostic.Kind.WARNING)
                .anyMatch(d -> d.getLineNumber() > 0 && d.getSource() != null);
    }

    @Test
    @DisplayName("Код без TODO проходит молча")
    void cleanCodeProducesNoDiagnostics() {
        CompilationHarness.Result result = compile("""
                package demo;

                public class Service {
                    public void run() {
                    }
                }
                """);

        assertThat(result.success()).isTrue();
        assertThat(result.warnings()).isEmpty();
    }
}

package ru.sprbut.m07;

import java.nio.file.Path;
import java.util.List;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;

@DisplayName("Слайд 60: процессор-анализатор, который ничего не генерирует")
final class TodoProcessorTest {

    @TempDir
    private Path workDir;

    private CompilationHarness.Result compile(String code) {
        return CompilationHarness.compile(
            this.workDir,
            List.of(new CompilationHarness.Source("demo.Service", code)),
            new TodoProcessor()
        );
    }

    @Test
    @DisplayName("непроходящий TODO сборку не роняет")
    void keepsBuildGreenOnPlainTodo() {
        assertThat(
            "non blocking todo cannot leave the build green",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;

                    public void run() {
                    }
                }
                """).success(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("текст TODO попадает в предупреждение вместе с именем элемента")
    void reportsTodoAsWarning() {
        assertThat(
            "todo text cannot reach the warning",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;
                }
                """).warnings(),
            hasItem(containsString("вынести в конфигурацию"))
        );
    }

    @Test
    @DisplayName("анализатор не генерирует ни одного файла")
    void generatesNothing() {
        assertThat(
            "analyser cannot stay free of generated sources",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;
                }
                """).generatedSources(),
            anEmptyMap()
        );
    }

    @Test
    @DisplayName("blocking = true роняет сборку — так работают Error Prone и NullAway")
    void failsBuildOnBlockingTodo() {
        assertThat(
            "blocking todo cannot fail the build",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                @Todo(value = "нельзя выпускать в прод", blocking = true)
                public class Service {
                }
                """).success(),
            equalTo(false)
        );
    }

    @Test
    @DisplayName("текст блокирующего TODO попадает в ошибку")
    void reportsBlockingTodoAsError() {
        assertThat(
            "blocking todo text cannot reach the error",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                @Todo(value = "нельзя выпускать в прод", blocking = true)
                public class Service {
                }
                """).errors(),
            hasItem(containsString("нельзя выпускать в прод"))
        );
    }

    @Test
    @DisplayName("диагностика ставится на конкретный элемент — с номером строки")
    void pointsAtTheElement() {
        assertThat(
            "diagnostic cannot point at a concrete line",
            compile("""
                package demo;

                import ru.sprbut.m07.api.Todo;

                public class Service {
                    @Todo("починить")
                    public void broken() {
                    }
                }
                """).diagnostics().stream()
                .filter(each -> each.getKind() == Diagnostic.Kind.WARNING)
                .anyMatch(each -> each.getLineNumber() > 0 && each.getSource() != null),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("код без TODO проходит молча")
    void staysSilentOnCleanCode() {
        assertThat(
            "clean code cannot pass without warnings",
            compile("""
                package demo;

                public class Service {
                    public void run() {
                    }
                }
                """).warnings(),
            emptyIterable()
        );
    }
}

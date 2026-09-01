/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07;

import java.nio.file.Path;
import java.util.List;
import javax.tools.Diagnostic;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Слайд 60: процессор-анализатор, который ничего не генерирует.
 * @since 1.0
 */
@DisplayName("Слайд 60: процессор-анализатор, который ничего не генерирует")
final class TodoProcessorTest {

    /**
     * Рабочий каталог.
     */
    @TempDir
    private Path workDir;

    @Test
    @DisplayName("непроходящий TODO сборку не роняет")
    void keepsBuildGreenOnPlainTodo() {
        MatcherAssert.assertThat(
            "non blocking todo cannot leave the build green",
            this.compile("""
                package demo;
                import ru.sprbut.m07.api.Todo;
                public class Service {

                    public void run() {
                    }
                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;
                }
                """).success(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("текст TODO попадает в предупреждение вместе с именем элемента")
    void reportsTodoAsWarning() {
        MatcherAssert.assertThat(
            "todo text cannot reach the warning",
            this.compile("""
                package demo;
                import ru.sprbut.m07.api.Todo;
                public class Service {

                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;
                }
                """).warnings(),
            Matchers.hasItem(Matchers.containsString("вынести в конфигурацию"))
        );
    }

    @Test
    @DisplayName("анализатор не генерирует ни одного файла")
    void generatesNothing() {
        MatcherAssert.assertThat(
            "analyser cannot stay free of generated sources",
            this.compile("""
                package demo;
                import ru.sprbut.m07.api.Todo;
                public class Service {

                    @Todo("вынести в конфигурацию")
                    private int timeout = 30;
                }
                """).generatedSources(),
            Matchers.anEmptyMap()
        );
    }

    @Test
    @DisplayName("blocking = true роняет сборку — так работают Error Prone и NullAway")
    void failsBuildOnBlockingTodo() {
        MatcherAssert.assertThat(
            "blocking todo cannot fail the build",
            this.compile("""
                package demo;
                import ru.sprbut.m07.api.Todo;
                @Todo(value = "нельзя выпускать в прод", blocking = true)
                public class Service {
                }
                """).success(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("текст блокирующего TODO попадает в ошибку")
    void reportsBlockingTodoAsError() {
        MatcherAssert.assertThat(
            "blocking todo text cannot reach the error",
            this.compile("""
                package demo;
                import ru.sprbut.m07.api.Todo;
                @Todo(value = "нельзя выпускать в прод", blocking = true)
                public class Service {
                }
                """).errors(),
            Matchers.hasItem(Matchers.containsString("нельзя выпускать в прод"))
        );
    }

    @Test
    @DisplayName("диагностика ставится на конкретный элемент — с номером строки")
    void pointsAtTheElement() {
        MatcherAssert.assertThat(
            "diagnostic cannot point at a concrete line",
            this.compile("""
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
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("код без TODO проходит молча")
    void staysSilentOnCleanCode() {
        MatcherAssert.assertThat(
            "clean code cannot pass without warnings",
            this.compile("""
                package demo;
                public class Service {

                    public void run() {
                    }
                }
                """).warnings(),
            Matchers.emptyIterable()
        );
    }

    private CompilationHarness.Result compile(final String code) {
        return CompilationHarness.compile(
            this.workDir,
            List.of(new CompilationHarness.Source("demo.Service", code)),
            new TodoProcessor()
        );
    }
}

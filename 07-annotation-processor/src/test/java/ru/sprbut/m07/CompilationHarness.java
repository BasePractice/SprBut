/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// стенд компиляции: полные имена классов и каталогов — часть предметной
// области javac, короткими именами их не заменить
// @checkstyle LocalFinalVariableNameCheck disable
// @checkstyle ParameterNameCheck disable
package ru.sprbut.m07;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Стенд для запуска процессора аннотаций из теста.
 *
 * <p>Процессор невозможно проверить обычным unit-тестом: он живёт внутри javac
 * и работает не с объектами, а с моделью исходного кода. Поэтому тест
 * <b>по-настоящему запускает компилятор</b> через {@code javax.tools} —
 * ровно то же самое, что делает Maven, только в памяти теста.</p>
 *
 * @since 1.0
 */
public final class CompilationHarness {

    private CompilationHarness() {
    }

    /**
     * Компилирует исходники с указанными процессорами.
     * @param workDir временный каталог теста
     * @param sources что компилировать
     * @param processor процессор, который нужно запустить
     * @param options дополнительные опции javac (например, {@code -Akey=value})
     * @return Результат компиляции
     * @checkstyle ParameterNumberCheck (8 lines)
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static Result compile(
        final Path workDir,
        final List<Source> sources,
        final Processor processor,
        final String... options
    ) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                "Нет системного компилятора: тест требует JDK, а не JRE"
            );
        }
        final Path classesDir = workDir.resolve("classes");
        final Path generatedDir = workDir.resolve("generated");
        try {
            Files.createDirectories(classesDir);
            Files.createDirectories(generatedDir);
        } catch (final IOException failure) {
            throw new UncheckedIOException(failure);
        }
        final Path sourceDir = workDir.resolve("src");
        final List<Path> files = sources.stream()
            .map(source -> source.writeTo(sourceDir))
            .toList();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (
            StandardJavaFileManager manager = compiler.getStandardFileManager(
                diagnostics, null, StandardCharsets.UTF_8
            )
        ) {
            final List<String> all = new ArrayList<>(
                List.of(
                    "-d", classesDir.toString(),
                    "-s", generatedDir.toString(),
                    "-classpath", System.getProperty("java.class.path"),
                    "--release", "17"
                )
            );
            all.addAll(List.of(options));
            final JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                manager,
                diagnostics,
                all,
                null,
                manager.getJavaFileObjectsFromPaths(files)
            );
            task.setProcessors(List.of(processor));
            return new Result(
                task.call(),
                diagnostics.getDiagnostics(),
                CompilationHarness.readGenerated(generatedDir),
                classesDir
            );
        } catch (final IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static Map<String, String> readGenerated(final Path generatedDir) {
        final Map<String, String> found = new LinkedHashMap<>();
        try (Stream<Path> walk = Files.walk(generatedDir)) {
            walk.filter(path -> path.toString().endsWith(".java"))
                .sorted(Comparator.comparing(Path::toString))
                .forEach(path -> CompilationHarness.read(generatedDir, path, found));
        } catch (final IOException failure) {
            throw new UncheckedIOException(failure);
        }
        return found;
    }

    // один сгенерированный файл: ключом становится полное имя класса
    private static void read(final Path root, final Path file, final Map<String, String> sink) {
        final String relative = root.relativize(file).toString();
        try {
            sink.put(
                relative.substring(0, relative.length() - ".java".length())
                    .replace(File.separatorChar, '.'),
                Files.readString(file, StandardCharsets.UTF_8)
            );
        } catch (final IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    /**
     * Исходник: полное имя класса и его текст.
     * @param qualifiedName Полное имя класса
     * @param code Текст исходника
     * @since 1.0
     */
    public record Source(String qualifiedName, String code) {

        Path writeTo(final Path root) {
            final Path file = root.resolve(
                String.format("%s.java", this.qualifiedName.replace('.', '/'))
            );
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, this.code, StandardCharsets.UTF_8);
            } catch (final IOException failure) {
                throw new UncheckedIOException(failure);
            }
            return file;
        }
    }

    /**
     * Результат компиляции вместе со сгенерированными исходниками и диагностикой.
     * @param success Успешно ли прошла компиляция
     * @param diagnostics Диагностика компилятора
     * @param generatedSources Сгенерированные исходники
     * @param classesDir Каталог с классами
     * @since 1.0
     */
    public record Result(
        boolean success,
        List<Diagnostic<? extends JavaFileObject>> diagnostics,
        Map<String, String> generatedSources,
        Path classesDir
    ) {

        /**
         * Сообщения.
         * @param kind Вид
         * @return Сообщения
         */
        public List<String> messages(final Diagnostic.Kind kind) {
            return this.diagnostics.stream()
                .filter(entry -> entry.getKind() == kind)
                .map(entry -> entry.getMessage(null))
                .toList();
        }

        /**
         * Сообщения об ошибках.
         * @return Сообщения об ошибках
         */
        public List<String> errors() {
            return this.messages(Diagnostic.Kind.ERROR);
        }

        /**
         * Предупреждения.
         * @return Предупреждения
         */
        public List<String> warnings() {
            return this.messages(Diagnostic.Kind.WARNING);
        }

        /**
         * Источник.
         * @param qualifiedName Имя
         * @return Источник
         */
        public String source(final String qualifiedName) {
            final String code = this.generatedSources.get(qualifiedName);
            if (code == null) {
                throw new AssertionError(
                    String.format(
                        "Не сгенерирован %s; есть только %s",
                        qualifiedName, this.generatedSources.keySet()
                    )
                );
            }
            return code;
        }

        /**
         * Загружает скомпилированный класс, чтобы проверить его поведение, а не текст.
         * @param qualifiedName Имя
         * @return Загруженный класс
         */
        @SuppressWarnings("PMD.UseProperClassLoader")
        public Class<?> load(final String qualifiedName) {
            try {
                return Class.forName(
                    qualifiedName,
                    true,
                    new URLClassLoader(
                        new URL[]{this.classesDir.toUri().toURL()},
                        CompilationHarness.class.getClassLoader()
                    )
                );
            } catch (final ReflectiveOperationException | IOException failure) {
                throw new AssertionError(
                    String.format("Не удалось загрузить %s", qualifiedName), failure
                );
            }
        }
    }
}

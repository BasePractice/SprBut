/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
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
     * @param workDir    временный каталог теста
     * @param sources    что компилировать
     * @param processor  процессор, который нужно запустить
     * @param options    дополнительные опции javac (например, {@code -Akey=value})
     * @return Компилирует исходники с указанными процессорами
     */
    public static Result compile(final Path workDir, final List<Source> sources, final Processor processor, final String... options) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Нет системного компилятора: тест требует JDK, а не JRE");
        }
        final Path sourceDir = workDir.resolve("src");
        final Path classesDir = workDir.resolve("classes");
        final Path generatedDir = workDir.resolve("generated");
        try {
            Files.createDirectories(classesDir);
            Files.createDirectories(generatedDir);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final List<Path> files = sources.stream().map(s -> s.writeTo(sourceDir)).toList();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(                         diagnostics, null, StandardCharsets.UTF_8
)) {
            final List<String> allOptions = new ArrayList<>(List.of(
                    "-d", classesDir.toString(),
                    "-s", generatedDir.toString(),
                    "-classpath", System.getProperty(                        "java.class.path"
),
                    "--release", "17"));
            allOptions.addAll(List.of(options));
            final JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    allOptions,
                    null,
                    fileManager.getJavaFileObjectsFromPaths(                        files
));
            task.setProcessors(List.of(processor));
            final boolean success = task.call();
            return new Result(                success, diagnostics.getDiagnostics(), readGenerated(generatedDir), classesDir
);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, String> readGenerated(final Path generatedDir) {
        final Map<String, String> result = new LinkedHashMap<>();
        try (Stream<Path> walk = Files.walk(generatedDir)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(p -> {
                        final String relative = generatedDir.relativize(p).toString();
                        final String name = relative.substring(0, relative.length() - ".java".length())
                                .replace(File.separatorChar, '.');
                        try {
                            result.put(name, Files.readString(p, StandardCharsets.UTF_8));
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    /**
     * Исходник: полное имя класса и его текст.
     */
    public record Source(String qualifiedName, String code) {

        Path writeTo(final Path root) {
            final Path file = root.resolve(this.qualifiedName.replace('.', '/') + ".java");
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, this.code, StandardCharsets.UTF_8);
                return file;
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Результат компиляции вместе со сгенерированными исходниками и диагностикой.
     */
    public record Result(boolean success,
                  List<Diagnostic<? extends JavaFileObject>> diagnostics,
                  Map<String, String> generatedSources,
                  Path classesDir) {

        /**
         * Сообщения.
         * @param kind Вид
         * @return Сообщения
         */
        public List<String> messages(final Diagnostic.Kind kind) {
            return this.diagnostics.stream()
                    .filter(d -> d.getKind() == kind)
                    .map(d -> d.getMessage(null))
                    .toList();
        }

        /**
         * Значение {@code errors}.
         * @return Значение {@code errors}
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
                throw new AssertionError("Не сгенерирован " + qualifiedName
                        + "; есть только " + this.generatedSources.keySet());
            }
            return code;
        }

        /**
         * Загружает скомпилированный класс, чтобы проверить его поведение, а не текст.
         * @param qualifiedName Имя
         * @return Загружает скомпилированный класс, чтобы проверить его поведение, а не текст
         */
        public Class<?> load(final String qualifiedName) {
            try {
                final URLClassLoader loader = new URLClassLoader(
                        new URL[]{this.classesDir.toUri().toURL()},
                        CompilationHarness.class.getClassLoader());
                return Class.forName(qualifiedName, true, loader);
            } catch (final Exception e) {
                throw new AssertionError("Не удалось загрузить " + qualifiedName, e);
            }
        }
    }
}

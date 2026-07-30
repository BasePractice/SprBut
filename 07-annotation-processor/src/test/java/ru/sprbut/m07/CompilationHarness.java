package ru.sprbut.m07;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
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

/**
 * Стенд для запуска процессора аннотаций из теста.
 * <p>
 * Процессор невозможно проверить обычным unit-тестом: он живёт внутри javac
 * и работает не с объектами, а с моделью исходного кода. Поэтому тест
 * <b>по-настоящему запускает компилятор</b> через {@code javax.tools} —
 * ровно то же самое, что делает Maven, только в памяти теста.
 */
public final class CompilationHarness {

    private CompilationHarness() {
    }

    /** Исходник: полное имя класса и его текст. */
    public record Source(String qualifiedName, String code) {

        Path writeTo(Path root) {
            Path file = root.resolve(qualifiedName.replace('.', '/') + ".java");
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, code, StandardCharsets.UTF_8);
                return file;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** Результат компиляции вместе со сгенерированными исходниками и диагностикой. */
    public record Result(boolean success,
                  List<Diagnostic<? extends JavaFileObject>> diagnostics,
                  Map<String, String> generatedSources,
                  Path classesDir) {

        public List<String> messages(Diagnostic.Kind kind) {
            return diagnostics.stream()
                    .filter(d -> d.getKind() == kind)
                    .map(d -> d.getMessage(null))
                    .toList();
        }

        public List<String> errors() {
            return messages(Diagnostic.Kind.ERROR);
        }

        public List<String> warnings() {
            return messages(Diagnostic.Kind.WARNING);
        }

        public String source(String qualifiedName) {
            String code = generatedSources.get(qualifiedName);
            if (code == null) {
                throw new AssertionError("Не сгенерирован " + qualifiedName
                        + "; есть только " + generatedSources.keySet());
            }
            return code;
        }

        /** Загружает скомпилированный класс, чтобы проверить его поведение, а не текст. */
        public Class<?> load(String qualifiedName) {
            try {
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{classesDir.toUri().toURL()},
                        CompilationHarness.class.getClassLoader());
                return Class.forName(qualifiedName, true, loader);
            } catch (Exception e) {
                throw new AssertionError("Не удалось загрузить " + qualifiedName, e);
            }
        }
    }

    /**
     * Компилирует исходники с указанными процессорами.
     *
     * @param workDir    временный каталог теста
     * @param sources    что компилировать
     * @param processor  процессор, который нужно запустить
     * @param options    дополнительные опции javac (например, {@code -Akey=value})
     */
    public static Result compile(Path workDir, List<Source> sources, Processor processor, String... options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Нет системного компилятора: тест требует JDK, а не JRE");
        }

        Path sourceDir = workDir.resolve("src");
        Path classesDir = workDir.resolve("classes");
        Path generatedDir = workDir.resolve("generated");
        try {
            Files.createDirectories(classesDir);
            Files.createDirectories(generatedDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<Path> files = sources.stream().map(s -> s.writeTo(sourceDir)).toList();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {

            List<String> allOptions = new ArrayList<>(List.of(
                    "-d", classesDir.toString(),
                    "-s", generatedDir.toString(),
                    "-classpath", System.getProperty("java.class.path"),
                    "--release", "17"));
            allOptions.addAll(List.of(options));

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    allOptions,
                    null,
                    fileManager.getJavaFileObjectsFromPaths(files));
            task.setProcessors(List.of(processor));

            boolean success = task.call();
            return new Result(success, diagnostics.getDiagnostics(),
                    readGenerated(generatedDir), classesDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, String> readGenerated(Path generatedDir) {
        Map<String, String> result = new LinkedHashMap<>();
        try (Stream<Path> walk = Files.walk(generatedDir)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(p -> {
                        String relative = generatedDir.relativize(p).toString();
                        String name = relative.substring(0, relative.length() - ".java".length())
                                .replace(java.io.File.separatorChar, '.');
                        try {
                            result.put(name, Files.readString(p, StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }
}

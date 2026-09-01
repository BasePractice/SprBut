/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07.extended;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.sprbut.m07.CompilationHarness;

/**
 * Расширенный пример: compile-time реестр вместо сканирования classpath.
 * @since 1.0
 */
@DisplayName("Расширенный пример: compile-time реестр вместо сканирования classpath")
final class RegistryProcessorTest {

    /**
     * Значение {@code REPO}.
     */
    private static final CompilationHarness.Source REPO = new CompilationHarness.Source(
            "demo.UserRepository", """
                    package demo;

                    import ru.sprbut.m07.api.Registered;

                    @Registered
                    public class UserRepository {

                        public String find() { return "пользователь"; }
                    }
                    """);

    /**
     * Значение {@code SERVICE}.
     * @since 1.0
     */
    private static final CompilationHarness.Source SERVICE = new CompilationHarness.Source(
            "demo.UserService", """
                    package demo;

                    import ru.sprbut.m07.api.Registered;

                    @Registered("users")
                    public class UserService {

                        public String describe() { return "сервис"; }
                    }
                    """);

    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    private CompilationHarness.Result compile(final List<CompilationHarness.Source> sources, final String... options) {
        return CompilationHarness.compile(RegistryProcessorTest.this.workDir, sources, new RegistryProcessor(), options);
    }

    /**
     * Генерация реестра.
     * @since 1.0
     */
    @Nested
    @DisplayName("Генерация реестра")
    final class Generation {

        @Test
        @DisplayName("Все помеченные классы собираются в один сгенерированный файл")
        void collectsEverythingIntoOneFile() {
            final CompilationHarness.Result result = compile(List.of(REPO, SERVICE));
            MatcherAssert.assertThat(
                "every annotated class cannot land in one generated file",
                result.generatedSources(),
                Matchers.aMapWithSize(1)
            );
        }

        @Test
        @DisplayName("в реестр попадает конструктор каждого класса")
        void registersEveryConstructor() {
            MatcherAssert.assertThat(
                "registry cannot reference the constructor of each class",
                compile(List.of(REPO, SERVICE)).source("ru.sprbut.generated.GeneratedRegistry"),
                Matchers.containsString("\"users\", UserService::new")
            );
        }

        @Test
        @DisplayName("JavaPoet сам расставляет импорты и форматирование")
        void javaPoetHandlesImports() {
            final String code = compile(List.of(REPO)).source("ru.sprbut.generated.GeneratedRegistry");
            MatcherAssert.assertThat(
                "JavaPoet cannot add the needed import",
                code,
                Matchers.containsString("import demo.UserRepository;")
            );
        }

        @Test
        @DisplayName("JavaPoet не импортирует java.lang — это лишнее")
        void skipsRedundantImport() {
            MatcherAssert.assertThat(
                "JavaPoet cannot skip the redundant java.lang import",
                compile(List.of(REPO)).source("ru.sprbut.generated.GeneratedRegistry"),
                Matchers.not(Matchers.containsString("import java.lang.String;"))
            );
        }

        @Test
        @DisplayName("Имя по умолчанию — имя класса с маленькой буквы, явное — из value")
        void resolvesNames() {
            final String code = compile(List.of(REPO, SERVICE)).source("ru.sprbut.generated.GeneratedRegistry");
            MatcherAssert.assertThat(
                "explicit name cannot replace the default one",
                code,
                Matchers.not(Matchers.containsString("\"userService\""))
            );
        }

        @Test
        @DisplayName(
            "Опции процессора -A меняют пакет и имя генерируемого класса"
        )
        void supportsProcessorOptions() {
            final CompilationHarness.Result result = compile(
                List.of(
                    REPO
                ), "-A" + RegistryProcessor.PACKAGE_OPTION + "=demo.gen",
                String.format("-A%s=Beans", RegistryProcessor.CLASS_OPTION)
            );
            MatcherAssert.assertThat(
                "processor options cannot change the generated class name",
                result.generatedSources(),
                Matchers.hasKey("demo.gen.Beans")
            );
        }

        @Test
        @DisplayName("Если ни одной аннотации нет, javac процессор вообще не вызывает")
        void processorIsNotInvokedWithoutAnnotations() {
            final RegistryProcessor processor = new RegistryProcessor();
            final CompilationHarness.Result result = CompilationHarness.compile(RegistryProcessorTest.this.workDir,
                    List.of(new CompilationHarness.Source(
                            "demo.Plain", "package demo; public class Plain {}")),
                    processor);
            MatcherAssert.assertThat(
                "processor cannot stay idle without its annotations",
                processor.rounds(),
                Matchers.equalTo(0)
            );
        }

        @Test
        @DisplayName("без аннотаций не генерируется ни одного файла")
        void generatesNothingWithoutAnnotations() {
            MatcherAssert.assertThat(
                "code without annotations cannot leave the output empty",
                CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir,
                    List.of(new CompilationHarness.Source(
                        "demo.Plain", "package demo; public class Plain {}")),
                    new RegistryProcessor()
                ).generatedSources(),
                Matchers.anEmptyMap()
            );
        }
    }

    /**
     * Сгенерированный реестр работает.
     * @since 1.0
     */
    @Nested
    @DisplayName("Сгенерированный реестр работает")
    final class Behaviour {

        @Test
        @DisplayName("create() создаёт объект через конструктор, а не через Class.forName")
        void createsInstancesWithoutReflection() throws Exception {
            final CompilationHarness.Result result = compile(List.of(REPO, SERVICE));
            final Class<?> registry = result.load("ru.sprbut.generated.GeneratedRegistry");
            final Object created = registry.getMethod("create", String.class).invoke(null, "users");
            MatcherAssert.assertThat(
                "generated registry cannot create the object without reflection",
                created.getClass().getName(),
                Matchers.equalTo("demo.UserService")
            );
        }

        @Test
        @DisplayName("names() отдаёт все зарегистрированные имена")
        void listsRegisteredNames() throws Exception {
            final Class<?> registry = compile(
                List.of(
                    REPO, SERVICE
                )
            )
                    .load(
                        "ru.sprbut.generated.GeneratedRegistry"
                    );
            @SuppressWarnings("unchecked")
            final Set<String> names = (Set<String>) registry.getMethod("names").invoke(null);
            MatcherAssert.assertThat(
                "registry cannot list every registered name",
                names,
                Matchers.containsInAnyOrder("userRepository", "users")
            );
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameFails() throws Exception {
            final Class<?> registry = compile(List.of(REPO)).load("ru.sprbut.generated.GeneratedRegistry");
            MatcherAssert.assertThat(
                "unknown name cannot be reported clearly",
                Assertions.assertThrows(
                    InvocationTargetException.class,
                    () -> registry.getMethod("create", String.class).invoke(null, "нет-такого")
                ).getCause().getMessage(),
                Matchers.equalTo("В реестре нет записи: нет-такого")
            );
        }
    }

    /**
     * Раунды обработки и валидация.
     * @since 1.0
     */
    @Nested
    @DisplayName("Раунды обработки и валидация")
    final class RoundsAndValidation {

        @Test
        @DisplayName("Процессор работает в нескольких раундах, но реестр пишет ровно один раз")
        void writesExactlyOnceAcrossRounds() {
            final RegistryProcessor processor = new RegistryProcessor();
            final CompilationHarness.Result result =
                    CompilationHarness.compile(
                        RegistryProcessorTest.this.workDir, List.of(REPO, SERVICE), processor
                    );
            MatcherAssert.assertThat(
                "processor cannot run in a working round and a final one",
                processor.rounds(),
                Matchers.greaterThanOrEqualTo(2)
            );
        }

        @Test
        @DisplayName("реестр пишется ровно один раз — повтор дал бы FilerException")
        void writesRegistryOnce() {
            MatcherAssert.assertThat(
                "registry cannot be written exactly once across the rounds",
                CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir, List.of(REPO, SERVICE), new RegistryProcessor()
                ).generatedSources(),
                Matchers.aMapWithSize(1)
            );
        }

        @Test
        @DisplayName("Реестр пишется в рабочем раунде, а не в последнем — иначе его нельзя импортировать")
        void writesEarlyEnoughToBeImportable() {
            final CompilationHarness.Result result = compile(List.of(REPO));
            // javac предупреждает про файлы последнего раунда; их нельзя
            // использовать из обычного кода. Этого предупреждения быть не должно.
            MatcherAssert.assertThat(
                "registry cannot be written early enough to be importable",
                result.warnings().stream().anyMatch(m -> m.contains("created in the last round")),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName(
            "Дубликат имени — ошибка сборки, а не молчаливая перезапись"
        )
        void rejectsDuplicateNames() {
            final CompilationHarness.Result result = compile(List.of(
                    new CompilationHarness.Source("demo.A", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered(
                                "dup"
                            ) public class A {} """),
                    new CompilationHarness.Source("demo.B", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered(
                                "dup"
                            ) public class B {} """)));
            MatcherAssert.assertThat(
                "duplicate name cannot fail the build instead of overwriting silently",
                result.errors().stream().anyMatch(m -> m.contains("уже занято")),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName(
            "Абстрактный класс и класс без подходящего конструктора отклоняются"
        )
        void rejectsUninstantiableClasses() {
            final CompilationHarness.Result abstractResult = compile(List.of(
                    new CompilationHarness.Source("demo.Abs", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered public abstract class Abs {}
                            """)));
            MatcherAssert.assertThat(
                "abstract class cannot be rejected by the registry processor",
                abstractResult.errors().stream().anyMatch(m -> m.contains("нечем создать")),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("класс без конструктора без параметров тоже отклоняется")
        void rejectsMissingNoArgConstructor() {
            final CompilationHarness.Result ctorResult = CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir.resolve(
                        "second"
                    ),
                    List.of(new CompilationHarness.Source("demo.NeedsArgs", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered public class NeedsArgs {

                                public NeedsArgs(String name) {}
                            }
                            """)),
                    new RegistryProcessor());
            MatcherAssert.assertThat(
                "class without a no-arg constructor cannot be rejected",
                ctorResult.errors().stream().anyMatch(m -> m.contains("конструктор без параметров")),
                Matchers.equalTo(true)
            );
        }
    }

    /**
     * Рабочий каталог.
     */
    @TempDir
    private Path workDir;

}

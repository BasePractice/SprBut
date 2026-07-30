package ru.sprbut.m07.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.sprbut.m07.CompilationHarness;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: compile-time реестр вместо сканирования classpath")
final class RegistryProcessorTest {

    @TempDir
    private Path workDir;

    private static final CompilationHarness.Source REPO = new CompilationHarness.Source(
            "demo.UserRepository", """
                    package demo;

                    import ru.sprbut.m07.api.Registered;

                    @Registered
                    public class UserRepository {
                        public String find() { return "пользователь"; }
                    }
                    """);

    private static final CompilationHarness.Source SERVICE = new CompilationHarness.Source(
            "demo.UserService", """
                    package demo;

                    import ru.sprbut.m07.api.Registered;

                    @Registered("users")
                    public class UserService {
                        public String describe() { return "сервис"; }
                    }
                    """);

    private CompilationHarness.Result compile(List<CompilationHarness.Source> sources, String... options) {
        return CompilationHarness.compile(RegistryProcessorTest.this.workDir, sources, new RegistryProcessor(), options);
    }

    @Nested
    @DisplayName("Генерация реестра")
    class Generation {

        @Test
        @DisplayName("Все помеченные классы собираются в один сгенерированный файл")
        void collectsEverythingIntoOneFile() {
            CompilationHarness.Result result = compile(List.of(REPO, SERVICE));

            assertThat(
                "every annotated class cannot land in one generated file",
                result.generatedSources(),
                aMapWithSize(1)
            );
        }

        @Test
        @DisplayName("в реестр попадает конструктор каждого класса")
        void registersEveryConstructor() {
            assertThat(
                "registry cannot reference the constructor of each class",
                compile(List.of(REPO, SERVICE)).source("ru.sprbut.generated.GeneratedRegistry"),
                containsString("\"users\", UserService::new")
            );
        }

        @Test
        @DisplayName("JavaPoet сам расставляет импорты и форматирование")
        void javaPoetHandlesImports() {
            String code = compile(List.of(REPO)).source("ru.sprbut.generated.GeneratedRegistry");

            assertThat(
                "JavaPoet cannot add the needed import",
                code,
                containsString("import demo.UserRepository;")
            );
        }

        @Test
        @DisplayName("JavaPoet не импортирует java.lang — это лишнее")
        void skipsRedundantImport() {
            assertThat(
                "JavaPoet cannot skip the redundant java.lang import",
                compile(List.of(REPO)).source("ru.sprbut.generated.GeneratedRegistry"),
                not(containsString("import java.lang.String;"))
            );
        }

        @Test
        @DisplayName("Имя по умолчанию — имя класса с маленькой буквы, явное — из value")
        void resolvesNames() {
            String code = compile(List.of(REPO, SERVICE)).source("ru.sprbut.generated.GeneratedRegistry");

            assertThat(
                "explicit name cannot replace the default one",
                code,
                not(containsString("\"userService\""))
            );
        }

        @Test
        @DisplayName("Опции процессора -A меняют пакет и имя генерируемого класса")
        void supportsProcessorOptions() {
            CompilationHarness.Result result = compile(List.of(REPO),
                    "-A" + RegistryProcessor.PACKAGE_OPTION + "=demo.gen",
                    "-A" + RegistryProcessor.CLASS_OPTION + "=Beans");

            assertThat(
                "processor options cannot change the generated class name",
                result.generatedSources(),
                hasKey("demo.gen.Beans")
            );
        }

        @Test
        @DisplayName("Если ни одной аннотации нет, javac процессор вообще не вызывает")
        void processorIsNotInvokedWithoutAnnotations() {
            RegistryProcessor processor = new RegistryProcessor();
            CompilationHarness.Result result = CompilationHarness.compile(RegistryProcessorTest.this.workDir,
                    List.of(new CompilationHarness.Source(
                            "demo.Plain", "package demo; public class Plain {}")),
                    processor);

            assertThat(
                "processor cannot stay idle without its annotations",
                processor.rounds(),
                equalTo(0)
            );
        }

        @Test
        @DisplayName("без аннотаций не генерируется ни одного файла")
        void generatesNothingWithoutAnnotations() {
            assertThat(
                "code without annotations cannot leave the output empty",
                CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir,
                    List.of(new CompilationHarness.Source(
                        "demo.Plain", "package demo; public class Plain {}")),
                    new RegistryProcessor()
                ).generatedSources(),
                anEmptyMap()
            );
        }
    }

    @Nested
    @DisplayName("Сгенерированный реестр работает")
    class Behaviour {

        @Test
        @DisplayName("create() создаёт объект через конструктор, а не через Class.forName")
        void createsInstancesWithoutReflection() throws Exception {
            CompilationHarness.Result result = compile(List.of(REPO, SERVICE));

            Class<?> registry = result.load("ru.sprbut.generated.GeneratedRegistry");

            Object created = registry.getMethod("create", String.class).invoke(null, "users");
            assertThat(
                "generated registry cannot create the object without reflection",
                created.getClass().getName(),
                equalTo("demo.UserService")
            );
        }

        @Test
        @DisplayName("names() отдаёт все зарегистрированные имена")
        void listsRegisteredNames() throws Exception {
            Class<?> registry = compile(List.of(REPO, SERVICE))
                    .load("ru.sprbut.generated.GeneratedRegistry");

            @SuppressWarnings("unchecked")
            Set<String> names = (Set<String>) registry.getMethod("names").invoke(null);

            assertThat(
                "registry cannot list every registered name",
                names,
                containsInAnyOrder("userRepository", "users")
            );
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameFails() throws Exception {
            Class<?> registry = compile(List.of(REPO)).load("ru.sprbut.generated.GeneratedRegistry");

            assertThat(
                "unknown name cannot be reported clearly",
                assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> registry.getMethod("create", String.class).invoke(null, "нет-такого")
                ).getCause().getMessage(),
                equalTo("В реестре нет записи: нет-такого")
            );
        }
    }

    @Nested
    @DisplayName("Раунды обработки и валидация")
    class RoundsAndValidation {

        @Test
        @DisplayName("Процессор работает в нескольких раундах, но реестр пишет ровно один раз")
        void writesExactlyOnceAcrossRounds() {
            RegistryProcessor processor = new RegistryProcessor();

            CompilationHarness.Result result =
                    CompilationHarness.compile(RegistryProcessorTest.this.workDir, List.of(REPO, SERVICE), processor);

            assertThat(
                "processor cannot run in a working round and a final one",
                processor.rounds(),
                greaterThanOrEqualTo(2)
            );
        }

        @Test
        @DisplayName("реестр пишется ровно один раз — повтор дал бы FilerException")
        void writesRegistryOnce() {
            assertThat(
                "registry cannot be written exactly once across the rounds",
                CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir, List.of(REPO, SERVICE), new RegistryProcessor()
                ).generatedSources(),
                aMapWithSize(1)
            );
        }

        @Test
        @DisplayName("Реестр пишется в рабочем раунде, а не в последнем — иначе его нельзя импортировать")
        void writesEarlyEnoughToBeImportable() {
            CompilationHarness.Result result = compile(List.of(REPO));

            // javac предупреждает про файлы последнего раунда; их нельзя
            // использовать из обычного кода. Этого предупреждения быть не должно.
            assertThat(
                "registry cannot be written early enough to be importable",
                result.warnings().stream().anyMatch(m -> m.contains("created in the last round")),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("Дубликат имени — ошибка сборки, а не молчаливая перезапись")
        void rejectsDuplicateNames() {
            CompilationHarness.Result result = compile(List.of(
                    new CompilationHarness.Source("demo.A", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered("dup") public class A {}
                            """),
                    new CompilationHarness.Source("demo.B", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered("dup") public class B {}
                            """)));

            assertThat(
                "duplicate name cannot fail the build instead of overwriting silently",
                result.errors().stream().anyMatch(m -> m.contains("уже занято")),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("Абстрактный класс и класс без подходящего конструктора отклоняются")
        void rejectsUninstantiableClasses() {
            CompilationHarness.Result abstractResult = compile(List.of(
                    new CompilationHarness.Source("demo.Abs", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered public abstract class Abs {}
                            """)));

            assertThat(
                "abstract class cannot be rejected by the registry processor",
                abstractResult.errors().stream().anyMatch(m -> m.contains("нечем создать")),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("класс без конструктора без параметров тоже отклоняется")
        void rejectsMissingNoArgConstructor() {
            CompilationHarness.Result ctorResult = CompilationHarness.compile(
                    RegistryProcessorTest.this.workDir.resolve("second"),
                    List.of(new CompilationHarness.Source("demo.NeedsArgs", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered public class NeedsArgs {
                                public NeedsArgs(String name) {}
                            }
                            """)),
                    new RegistryProcessor());
            assertThat(
                "class without a no-arg constructor cannot be rejected",
                ctorResult.errors().stream().anyMatch(m -> m.contains("конструктор без параметров")),
                equalTo(true)
            );

        }
    }
}

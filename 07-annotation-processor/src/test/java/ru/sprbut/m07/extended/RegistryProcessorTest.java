package ru.sprbut.m07.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.sprbut.m07.CompilationHarness;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: compile-time реестр вместо сканирования classpath")
class RegistryProcessorTest {

    @TempDir
    Path workDir;

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
        return CompilationHarness.compile(workDir, sources, new RegistryProcessor(), options);
    }

    @Nested
    @DisplayName("Генерация реестра")
    class Generation {

        @Test
        @DisplayName("Все помеченные классы собираются в один сгенерированный файл")
        void collectsEverythingIntoOneFile() {
            CompilationHarness.Result result = compile(List.of(REPO, SERVICE));

            assertThat(result.success()).as(result.errors().toString()).isTrue();
            assertThat(result.generatedSources()).containsOnlyKeys("ru.sprbut.generated.GeneratedRegistry");

            String code = result.source("ru.sprbut.generated.GeneratedRegistry");
            assertThat(code)
                    .contains("\"userRepository\", UserRepository::new")
                    .contains("\"users\", UserService::new");
        }

        @Test
        @DisplayName("JavaPoet сам расставляет импорты и форматирование")
        void javaPoetHandlesImports() {
            String code = compile(List.of(REPO)).source("ru.sprbut.generated.GeneratedRegistry");

            assertThat(code)
                    .contains("package ru.sprbut.generated;")
                    .contains("import demo.UserRepository;")
                    .contains("import java.util.Map;")
                    .doesNotContain("import java.lang.String;");
        }

        @Test
        @DisplayName("Имя по умолчанию — имя класса с маленькой буквы, явное — из value")
        void resolvesNames() {
            String code = compile(List.of(REPO, SERVICE)).source("ru.sprbut.generated.GeneratedRegistry");

            assertThat(code).contains("\"userRepository\"").contains("\"users\"")
                    .doesNotContain("\"userService\"");
        }

        @Test
        @DisplayName("Опции процессора -A меняют пакет и имя генерируемого класса")
        void supportsProcessorOptions() {
            CompilationHarness.Result result = compile(List.of(REPO),
                    "-A" + RegistryProcessor.PACKAGE_OPTION + "=demo.gen",
                    "-A" + RegistryProcessor.CLASS_OPTION + "=Beans");

            assertThat(result.generatedSources()).containsOnlyKeys("demo.gen.Beans");
        }

        @Test
        @DisplayName("Если ни одной аннотации нет, javac процессор вообще не вызывает")
        void processorIsNotInvokedWithoutAnnotations() {
            RegistryProcessor processor = new RegistryProcessor();
            CompilationHarness.Result result = CompilationHarness.compile(workDir,
                    List.of(new CompilationHarness.Source(
                            "demo.Plain", "package demo; public class Plain {}")),
                    processor);

            assertThat(result.success()).isTrue();
            // Процессор запускается, только если в раунде есть аннотации из
            // @SupportedAnnotationTypes. Чтобы вызываться всегда, надо объявить "*".
            assertThat(processor.rounds()).isZero();
            assertThat(result.generatedSources()).isEmpty();
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

            assertThat(registry.getMethod("size").invoke(null)).isEqualTo(2);

            Object created = registry.getMethod("create", String.class).invoke(null, "users");
            assertThat(created.getClass().getName()).isEqualTo("demo.UserService");
            assertThat(created.getClass().getMethod("describe").invoke(created)).isEqualTo("сервис");
        }

        @Test
        @DisplayName("names() отдаёт все зарегистрированные имена")
        void listsRegisteredNames() throws Exception {
            Class<?> registry = compile(List.of(REPO, SERVICE))
                    .load("ru.sprbut.generated.GeneratedRegistry");

            @SuppressWarnings("unchecked")
            Set<String> names = (Set<String>) registry.getMethod("names").invoke(null);

            assertThat(names).containsExactlyInAnyOrder("userRepository", "users");
        }

        @Test
        @DisplayName("Неизвестное имя даёт понятную ошибку")
        void unknownNameFails() throws Exception {
            Class<?> registry = compile(List.of(REPO)).load("ru.sprbut.generated.GeneratedRegistry");

            assertThat(registry.getMethod("create", String.class))
                    .satisfies(method -> org.assertj.core.api.Assertions
                            .assertThatThrownBy(() -> method.invoke(null, "нет-такого"))
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasRootCauseMessage("В реестре нет записи: нет-такого"));
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
                    CompilationHarness.compile(workDir, List.of(REPO, SERVICE), processor);

            assertThat(processor.rounds())
                    .as("рабочий раунд плюс завершающий")
                    .isGreaterThanOrEqualTo(2);
            // Повторная запись того же файла привела бы к FilerException
            assertThat(result.success()).isTrue();
            assertThat(result.generatedSources()).hasSize(1);
        }

        @Test
        @DisplayName("Реестр пишется в рабочем раунде, а не в последнем — иначе его нельзя импортировать")
        void writesEarlyEnoughToBeImportable() {
            CompilationHarness.Result result = compile(List.of(REPO));

            // javac предупреждает про файлы последнего раунда; их нельзя
            // использовать из обычного кода. Этого предупреждения быть не должно.
            assertThat(result.warnings())
                    .noneMatch(m -> m.contains("created in the last round"));
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

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(m -> m.contains("уже занято"));
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

            assertThat(abstractResult.errors()).anyMatch(m -> m.contains("нечем создать"));

            CompilationHarness.Result ctorResult = CompilationHarness.compile(
                    workDir.resolve("second"),
                    List.of(new CompilationHarness.Source("demo.NeedsArgs", """
                            package demo;
                            import ru.sprbut.m07.api.Registered;
                            @Registered public class NeedsArgs {
                                public NeedsArgs(String name) {}
                            }
                            """)),
                    new RegistryProcessor());

            assertThat(ctorResult.errors()).anyMatch(m -> m.contains("конструктор без параметров"));
        }
    }
}

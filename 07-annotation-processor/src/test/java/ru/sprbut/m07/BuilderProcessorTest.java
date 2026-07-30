package ru.sprbut.m07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайды 57–64: AbstractProcessor генерирует и анализирует код")
final class BuilderProcessorTest {

    @TempDir
    private Path workDir;

    private static final CompilationHarness.Source VALID_BEAN = new CompilationHarness.Source(
            "demo.Customer", """
                    package demo;

                    import ru.sprbut.m07.api.GenerateBuilder;

                    @GenerateBuilder
                    public class Customer {
                        private String name;
                        private int age;
                        private boolean vip;
                        private static String ignored;

                        public String getName() { return name; }
                        public void setName(String name) { this.name = name; }
                        public int getAge() { return age; }
                        public void setAge(int age) { this.age = age; }
                        public boolean isVip() { return vip; }
                        public void setVip(boolean vip) { this.vip = vip; }
                    }
                    """);

    private CompilationHarness.Result compile(CompilationHarness.Source... sources) {
        return CompilationHarness.compile(this.workDir, List.of(sources), new BuilderProcessor());
    }

    @Nested
    @DisplayName("Генерация исходного кода")
    class Generation {

        @Test
        @DisplayName("Для помеченного класса рядом появляется CustomerBuilder")
        void generatesBuilder() {
            CompilationHarness.Result result = compile(VALID_BEAN);

            assertThat(
                "annotated class cannot get its builder generated",
                result.generatedSources(),
                hasKey("demo.CustomerBuilder")
            );
        }

        @Test
        @DisplayName("У билдера есть fluent-метод на каждое нестатическое поле")
        void generatesFluentSetters() {
            String code = compile(VALID_BEAN).source("demo.CustomerBuilder");

            assertThat(
                "builder cannot get a fluent method per field",
                code,
                containsString("public CustomerBuilder name(java.lang.String value)")
            );
        }

        @Test
        @DisplayName("статическое поле в билдер не попадает")
        void skipsStaticField() {
            assertThat(
                "static field cannot stay out of the builder",
                compile(VALID_BEAN).source("demo.CustomerBuilder"),
                not(containsString("ignored"))
            );
        }

        @Test
        @DisplayName("build() создаёт объект конструктором без параметров и зовёт сеттеры")
        void generatesBuildMethod() {
            String code = compile(VALID_BEAN).source("demo.CustomerBuilder");

            assertThat(
                "build method cannot create the object with a no-arg constructor",
                code,
                containsString("Customer result = new Customer();")
            );
        }

        @Test
        @DisplayName("build() наполняет объект через сеттеры")
        void callsSetters() {
            assertThat(
                "build method cannot fill the object through setters",
                compile(VALID_BEAN).source("demo.CustomerBuilder"),
                containsString("result.setName(this.name);")
            );
        }

        @Test
        @DisplayName("Сгенерированный код компилируется и реально работает")
        void generatedCodeActuallyRuns() throws Exception {
            CompilationHarness.Result result = compile(VALID_BEAN);

            Class<?> builderClass = result.load("demo.CustomerBuilder");
            Object builder = builderClass.getMethod("create").invoke(null);
            builder = builderClass.getMethod("name", String.class).invoke(builder, "Иванов");
            builder = builderClass.getMethod("age", int.class).invoke(builder, 42);
            Object customer = builderClass.getMethod("build").invoke(builder);

            assertThat(
                "generated code cannot actually build the object",
                customer.getClass().getMethod("getName").invoke(customer),
                equalTo("Иванов")
            );
        }

        @Test
        @DisplayName("Суффикс имени берётся из элемента аннотации")
        void respectsSuffixElement() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Order", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder(suffix = "Factory")
                            public class Order {
                                private String id;
                                public String getId() { return id; }
                                public void setId(String id) { this.id = id; }
                            }
                            """));

            assertThat(
                "suffix element cannot rename the generated class",
                result.generatedSources(),
                hasKey("demo.OrderFactory")
            );
        }

        @Test
        @DisplayName("Класс без аннотации не трогается вовсе")
        void ignoresUnannotatedClasses() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Plain", """
                            package demo;
                            public class Plain {
                                private String name;
                                public String getName() { return name; }
                                public void setName(String name) { this.name = name; }
                            }
                            """));

            assertThat(
                "unannotated class cannot be left alone",
                result.generatedSources(),
                anEmptyMap()
            );
        }
    }

    @Nested
    @DisplayName("Анализ исходного кода")
    class Analysis {

        @Test
        @DisplayName("Без конструктора без параметров сборка падает с внятной ошибкой")
        void requiresNoArgConstructor() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.NoDefaultCtor", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder
                            public class NoDefaultCtor {
                                private String name;
                                public NoDefaultCtor(String name) { this.name = name; }
                                public String getName() { return name; }
                                public void setName(String name) { this.name = name; }
                            }
                            """));

            assertThat(
                "missing no-arg constructor cannot fail the build with an explanation",
                result.errors(),
                hasItem(containsString("публичный конструктор без параметров"))
            );
        }

        @Test
        @DisplayName("Поле без сеттера — ошибка с точным указанием, чего не хватает")
        void requiresSetterForEveryField() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.NoSetter", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder
                            public class NoSetter {
                                private String name;
                                public String getName() { return name; }
                            }
                            """));

            assertThat(
                "missing setter cannot be named in the error",
                result.errors(),
                hasItem(containsString("setName"))
            );
        }

        @Test
        @DisplayName("Абстрактный класс отклоняется")
        void rejectsAbstractClasses() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Abstract", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder
                            public abstract class Abstract {
                            }
                            """));

            assertThat(
                "abstract class cannot be rejected with an explanation",
                result.errors(),
                hasItem(containsString("абстрактному классу"))
            );
        }

        @Test
        @DisplayName("Класс без свойств — предупреждение, но сборка проходит")
        void warnsOnEmptyClass() {
            CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Empty", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder
                            public class Empty {
                            }
                            """));

            assertThat(
                "empty class cannot yield a warning instead of an error",
                result.warnings(),
                hasItem(containsString("билдер будет пустым"))
            );
        }
    }

    @Nested
    @DisplayName("Как процессор видит код")
    class ProcessingModel {

        @Test
        @DisplayName("Обрабатываются все помеченные классы за одну сборку")
        void processesEveryAnnotatedClass() {
            CompilationHarness.Result result = compile(VALID_BEAN, new CompilationHarness.Source(
                    "demo.Order", """
                            package demo;

                            import ru.sprbut.m07.api.GenerateBuilder;

                            @GenerateBuilder
                            public class Order {
                                private String id;
                                public String getId() { return id; }
                                public void setId(String id) { this.id = id; }
                            }
                            """));

            assertThat(
                "every annotated class cannot be processed in one build",
                result.generatedSources(),
                hasKey("demo.OrderBuilder")
            );
        }

        @Test
        @DisplayName("Аннотация с retention SOURCE в байткод не попадает")
        void sourceRetentionLeavesNoTrace() {
            CompilationHarness.Result result = compile(VALID_BEAN);

            Class<?> customer = result.load("demo.Customer");

            assertThat(
                "source retained annotation cannot vanish from the bytecode",
                customer.getAnnotations(),
                emptyArray()
            );
        }

        @Test
        @DisplayName("Процессор запускается минимум в двух раундах: рабочем и завершающем")
        void runsInMultipleRounds() {
            BuilderProcessor processor = new BuilderProcessor();
            CompilationHarness.compile(BuilderProcessorTest.this.workDir, List.of(VALID_BEAN), processor);
            assertThat(
                "processor cannot run in at least two rounds",
                processor.rounds(),
                greaterThanOrEqualTo(2)
            );
        }
    }
}

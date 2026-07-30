package ru.sprbut.m07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 57–64: AbstractProcessor генерирует и анализирует код")
class BuilderProcessorTest {

    @TempDir
    Path workDir;

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
        return CompilationHarness.compile(workDir, List.of(sources), new BuilderProcessor());
    }

    @Nested
    @DisplayName("Генерация исходного кода")
    class Generation {

        @Test
        @DisplayName("Для помеченного класса рядом появляется CustomerBuilder")
        void generatesBuilder() {
            CompilationHarness.Result result = compile(VALID_BEAN);

            assertThat(result.success()).as(result.errors().toString()).isTrue();
            assertThat(result.generatedSources()).containsKey("demo.CustomerBuilder");
        }

        @Test
        @DisplayName("У билдера есть fluent-метод на каждое нестатическое поле")
        void generatesFluentSetters() {
            String code = compile(VALID_BEAN).source("demo.CustomerBuilder");

            assertThat(code)
                    .contains("public CustomerBuilder name(java.lang.String value)")
                    .contains("public CustomerBuilder age(int value)")
                    .contains("public CustomerBuilder vip(boolean value)")
                    .doesNotContain("ignored");
        }

        @Test
        @DisplayName("build() создаёт объект конструктором без параметров и зовёт сеттеры")
        void generatesBuildMethod() {
            String code = compile(VALID_BEAN).source("demo.CustomerBuilder");

            assertThat(code)
                    .contains("Customer result = new Customer();")
                    .contains("result.setName(this.name);")
                    .contains("result.setAge(this.age);")
                    .contains("return result;");
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

            assertThat(customer.getClass().getMethod("getName").invoke(customer)).isEqualTo("Иванов");
            assertThat(customer.getClass().getMethod("getAge").invoke(customer)).isEqualTo(42);
            assertThat(customer.getClass().getMethod("isVip").invoke(customer)).isEqualTo(false);
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

            assertThat(result.generatedSources()).containsKey("demo.OrderFactory");
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

            assertThat(result.success()).isTrue();
            assertThat(result.generatedSources()).isEmpty();
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

            assertThat(result.success()).isFalse();
            assertThat(result.errors())
                    .anyMatch(m -> m.contains("публичный конструктор без параметров"));
            assertThat(result.generatedSources()).isEmpty();
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

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(m -> m.contains("setName"));
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

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(m -> m.contains("абстрактному классу"));
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

            assertThat(result.success()).isTrue();
            assertThat(result.warnings()).anyMatch(m -> m.contains("билдер будет пустым"));
            assertThat(result.generatedSources()).containsKey("demo.EmptyBuilder");
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

            assertThat(result.generatedSources())
                    .containsKeys("demo.CustomerBuilder", "demo.OrderBuilder");
        }

        @Test
        @DisplayName("Аннотация с retention SOURCE в байткод не попадает")
        void sourceRetentionLeavesNoTrace() {
            CompilationHarness.Result result = compile(VALID_BEAN);

            Class<?> customer = result.load("demo.Customer");

            assertThat(customer.getAnnotations())
                    .as("@GenerateBuilder не должна быть видна в runtime")
                    .isEmpty();
        }

        @Test
        @DisplayName("Процессор запускается минимум в двух раундах: рабочем и завершающем")
        void runsInMultipleRounds() {
            BuilderProcessor processor = new BuilderProcessor();
            CompilationHarness.compile(workDir, List.of(VALID_BEAN), processor);

            assertThat(processor.rounds()).isGreaterThanOrEqualTo(2);
        }
    }
}

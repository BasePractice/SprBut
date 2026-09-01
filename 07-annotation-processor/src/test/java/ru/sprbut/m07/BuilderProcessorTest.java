/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Слайды 57–64: AbstractProcessor генерирует и анализирует код.
 * @since 1.0
 */
@DisplayName("Слайды 57–64: AbstractProcessor генерирует и анализирует код")
final class BuilderProcessorTest {
    /**
     * Значение {@code VALID_BEAN}.
     * @since 1.0
     */
    private static final CompilationHarness.Source VALID_BEAN = new CompilationHarness.Source(
            "demo.Customer", """
                    package demo;

                    import ru.sprbut.m07.api.GenerateBuilder;

                    @GenerateBuilder
                    public class Customer {
                        private static String ignored;

                        private String name;

                        private int age;

                        private boolean vip;

                        public String getName() { return name; }

                        public void setName(String name) { this.name = name; }

                        public int getAge() { return age; }

                        public void setAge(int age) { this.age = age; }

                        public boolean isVip() { return vip; }

                        public void setVip(boolean vip) { this.vip = vip; }
                    }
                    """);

    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    private CompilationHarness.Result compile(final CompilationHarness.Source... sources) {
        return CompilationHarness.compile(this.workDir, List.of(sources), new BuilderProcessor());
    }

    @Nested
/**
 * Генерация исходного кода.
 * @since 1.0
 */
    @DisplayName("Генерация исходного кода")
    final class Generation {

        @Test
        @DisplayName("Для помеченного класса рядом появляется CustomerBuilder")
        void generatesBuilder() {
            final CompilationHarness.Result result = compile(VALID_BEAN);
            MatcherAssert.assertThat(
                "annotated class cannot get its builder generated",
                result.generatedSources(),
                Matchers.hasKey("demo.CustomerBuilder")
            );
        }

        @Test
        @DisplayName("У билдера есть fluent-метод на каждое нестатическое поле")
        void generatesFluentSetters() {
            final String code = compile(VALID_BEAN).source("demo.CustomerBuilder");
            MatcherAssert.assertThat(
                "builder cannot get a fluent method per field",
                code,
                Matchers.containsString("public CustomerBuilder name(java.lang.String value)")
            );
        }

        @Test
        @DisplayName("статическое поле в билдер не попадает")
        void skipsStaticField() {
            MatcherAssert.assertThat(
                "static field cannot stay out of the builder",
                compile(VALID_BEAN).source("demo.CustomerBuilder"),
                Matchers.not(Matchers.containsString("ignored"))
            );
        }

        @Test
        @DisplayName("build() создаёт объект конструктором без параметров и зовёт сеттеры")
        void generatesBuildMethod() {
            final String code = compile(VALID_BEAN).source("demo.CustomerBuilder");
            MatcherAssert.assertThat(
                "build method cannot create the object with a no-arg constructor",
                code,
                Matchers.containsString("Customer result = new Customer();")
            );
        }

        @Test
        @DisplayName("build() наполняет объект через сеттеры")
        void callsSetters() {
            MatcherAssert.assertThat(
                "build method cannot fill the object through setters",
                compile(VALID_BEAN).source("demo.CustomerBuilder"),
                Matchers.containsString("result.setName(this.name);")
            );
        }

        @Test
        @DisplayName("Сгенерированный код компилируется и реально работает")
        void generatedCodeActuallyRuns() throws Exception {
            final CompilationHarness.Result result = compile(VALID_BEAN);
            final Class<?> builderClass = result.load("demo.CustomerBuilder");
            Object builder = builderClass.getMethod("create").invoke(null);
            builder = builderClass.getMethod("name", String.class).invoke(builder, "Иванов");
            builder = builderClass.getMethod("age", int.class).invoke(builder, 42);
            final Object customer = builderClass.getMethod("build").invoke(builder);
            MatcherAssert.assertThat(
                "generated code cannot actually build the object",
                customer.getClass().getMethod("getName").invoke(customer),
                Matchers.equalTo("Иванов")
            );
        }

        @Test
        @DisplayName("Суффикс имени берётся из элемента аннотации")
        void respectsSuffixElement() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
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
            MatcherAssert.assertThat(
                "suffix element cannot rename the generated class",
                result.generatedSources(),
                Matchers.hasKey("demo.OrderFactory")
            );
        }

        @Test
        @DisplayName("Класс без аннотации не трогается вовсе")
        void ignoresUnannotatedClasses() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Plain", """
                            package demo;
                            public class Plain {
                                private String name;
                                public String getName() { return name; }
                                public void setName(String name) { this.name = name; }
                            }
                            """));
            MatcherAssert.assertThat(
                "unannotated class cannot be left alone",
                result.generatedSources(),
                Matchers.anEmptyMap()
            );
        }
    }

    @Nested
/**
 * Анализ исходного кода.
 * @since 1.0
 */
    @DisplayName("Анализ исходного кода")
    final class Analysis {

        @Test
        @DisplayName("Без конструктора без параметров сборка падает с внятной ошибкой")
        void requiresNoArgConstructor() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
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
            MatcherAssert.assertThat(
                "missing no-arg constructor cannot fail the build with an explanation",
                result.errors(),
                Matchers.hasItem(Matchers.containsString("публичный конструктор без параметров"))
            );
        }

        @Test
        @DisplayName("Поле без сеттера — ошибка с точным указанием, чего не хватает")
        void requiresSetterForEveryField() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.NoSetter", """
                            package demo;
                            import ru.sprbut.m07.api.GenerateBuilder;
                            @GenerateBuilder
                            public class NoSetter {
                                private String name;
                                public String getName() { return name; }
                            }
                            """));
            MatcherAssert.assertThat(
                "missing setter cannot be named in the error",
                result.errors(),
                Matchers.hasItem(Matchers.containsString("setName"))
            );
        }

        @Test
        @DisplayName("Абстрактный класс отклоняется")
        void rejectsAbstractClasses() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Abstract", """
                            package demo;
                            import ru.sprbut.m07.api.GenerateBuilder;
                            @GenerateBuilder
                            public abstract class Abstract {
                            }
                            """));
            MatcherAssert.assertThat(
                "abstract class cannot be rejected with an explanation",
                result.errors(),
                Matchers.hasItem(Matchers.containsString("абстрактному классу"))
            );
        }

        @Test
        @DisplayName("Класс без свойств — предупреждение, но сборка проходит")
        void warnsOnEmptyClass() {
            final CompilationHarness.Result result = compile(new CompilationHarness.Source(
                    "demo.Empty", """
                            package demo;
                            import ru.sprbut.m07.api.GenerateBuilder;
                            @GenerateBuilder
                            public class Empty {
                            }
                            """));
            MatcherAssert.assertThat(
                "empty class cannot yield a warning instead of an error",
                result.warnings(),
                Matchers.hasItem(Matchers.containsString("билдер будет пустым"))
            );
        }
    }

    @Nested
/**
 * Как процессор видит код.
 * @since 1.0
 */
    @DisplayName("Как процессор видит код")
    final class ProcessingModel {

        @Test
        @DisplayName("Обрабатываются все помеченные классы за одну сборку")
        void processesEveryAnnotatedClass() {
            final CompilationHarness.Result result = compile(VALID_BEAN, new CompilationHarness.Source(
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
            MatcherAssert.assertThat(
                "every annotated class cannot be processed in one build",
                result.generatedSources(),
                Matchers.hasKey("demo.OrderBuilder")
            );
        }

        @Test
        @DisplayName("Аннотация с retention SOURCE в байткод не попадает")
        void sourceRetentionLeavesNoTrace() {
            final CompilationHarness.Result result = compile(VALID_BEAN);
            final Class<?> customer = result.load("demo.Customer");
            MatcherAssert.assertThat(
                "source retained annotation cannot vanish from the bytecode",
                customer.getAnnotations(),
                Matchers.emptyArray()
            );
        }

        @Test
        @DisplayName("Процессор запускается минимум в двух раундах: рабочем и завершающем")
        void runsInMultipleRounds() {
            final BuilderProcessor processor = new BuilderProcessor();
            CompilationHarness.compile(BuilderProcessorTest.this.workDir, List.of(VALID_BEAN), processor);
            MatcherAssert.assertThat(
                "processor cannot run in at least two rounds",
                processor.rounds(),
                Matchers.greaterThanOrEqualTo(2)
            );
        }
    }

    /**
     * Рабочий каталог.
     */
    @TempDir
    private Path workDir;

}

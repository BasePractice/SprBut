package ru.sprbut.m19;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration;
import ru.sprbut.m19.greeter.Greeter;
import ru.sprbut.m19.greeter.GreeterProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ApplicationContextRunner} — штатный инструмент для тестов автоконфигурации.
 * Он поднимает контекст на каждый сценарий, но лёгкий и без веб-окружения.
 */
@DisplayName("Слайды 173–178 (СХЕМА 12): starter → imports → условия → бин")
class GreeterAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class));

    @Nested
    @DisplayName("Бин появляется сам")
    class WorksOutOfTheBox {

        @Test
        @DisplayName("Без единой строки конфигурации в контексте есть готовый Greeter")
        void greeterAppearsWithoutConfiguration() {
            runner.run(context -> {
                assertThat(context).hasSingleBean(Greeter.class);
                assertThat(context.getBean(Greeter.class).greet("Мир"))
                        .isEqualTo("Привет, Мир!");
            });
        }

        @Test
        @DisplayName("Настройки читаются из свойств с префиксом стартера")
        void propertiesAreBound() {
            runner.withPropertyValues(
                            "sprbut.greeter.template=Здравствуйте, {name}",
                            "sprbut.greeter.shout=true")
                    .run(context -> assertThat(context.getBean(Greeter.class).greet("Иван"))
                            .isEqualTo("ЗДРАВСТВУЙТЕ, ИВАН"));
        }

        @Test
        @DisplayName("@EnableConfigurationProperties регистрирует класс настроек")
        void propertiesBeanIsRegistered() {
            runner.run(context -> assertThat(context).hasSingleBean(GreeterProperties.class));
        }
    }

    @Nested
    @DisplayName("Слайд 176: условия")
    class Conditions {

        @Test
        @DisplayName("@ConditionalOnProperty: выключатель отключает весь стартер")
        void canBeTurnedOff() {
            runner.withPropertyValues("sprbut.greeter.enabled=false")
                    .run(context -> assertThat(context).doesNotHaveBean(Greeter.class));
        }

        @Test
        @DisplayName("matchIfMissing = true: без свойства стартер включён")
        void enabledByDefault() {
            runner.run(context -> assertThat(context).hasSingleBean(Greeter.class));
        }

        @Test
        @DisplayName("@ConditionalOnClass: без нужного класса автоконфигурация не применяется")
        void requiresTheLibraryOnClasspath() {
            runner.withClassLoader(new org.springframework.boot.test.context.FilteredClassLoader(
                            Greeter.class))
                    .run(context -> assertThat(context).doesNotHaveBean("greeter"));
        }
    }

    @Nested
    @DisplayName("Слайд 177: свой бин переопределяет автоконфигурацию")
    class UserBeanWins {

        @Configuration
        static class UserConfig {

            @Bean
            Greeter greeter() {
                return new Greeter() {
                    @Override
                    public String greet(String name) {
                        return "Здарова, " + name;
                    }

                    @Override
                    public String flavour() {
                        return "пользовательский";
                    }
                };
            }
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean уступает пользовательскому бину")
        void autoConfigurationBacksOff() {
            runner.withUserConfiguration(UserConfig.class).run(context -> {
                assertThat(context).hasSingleBean(Greeter.class);
                assertThat(context.getBean(Greeter.class).flavour()).isEqualTo("пользовательский");
                assertThat(context.getBean(Greeter.class).greet("Мир")).isEqualTo("Здарова, Мир");
            });
        }

        @Test
        @DisplayName("Настройки при этом всё равно биндятся — стартер не отключился целиком")
        void propertiesStillBind() {
            runner.withUserConfiguration(UserConfig.class)
                    .withPropertyValues("sprbut.greeter.template=неважно")
                    .run(context -> assertThat(context.getBean(GreeterProperties.class).getTemplate())
                            .isEqualTo("неважно"));
        }
    }

    @Nested
    @DisplayName("Слайд 175: регистрация через AutoConfiguration.imports")
    class Registration {

        @Test
        @DisplayName("Файл регистрации существует и содержит нашу автоконфигурацию")
        void importsFileListsTheAutoConfiguration() throws Exception {
            var url = getClass().getClassLoader().getResource(
                    "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");

            assertThat(url).isNotNull();
            assertThat(new String(url.openStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8))
                    .contains("ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration");
        }

        @Test
        @DisplayName("@AutoConfiguration — это @Configuration(proxyBeanMethods = false)")
        void autoConfigurationIsALiteConfiguration() {
            var annotation = GreeterAutoConfiguration.class
                    .getAnnotation(org.springframework.boot.autoconfigure.AutoConfiguration.class);

            assertThat(annotation).isNotNull();
            assertThat(GreeterAutoConfiguration.class.isAnnotationPresent(
                    org.springframework.boot.autoconfigure.condition.ConditionalOnClass.class))
                    .isTrue();
        }
    }
}

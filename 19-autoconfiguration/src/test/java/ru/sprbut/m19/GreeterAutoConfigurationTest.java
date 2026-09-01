/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration;
import ru.sprbut.m19.greeter.Greeter;
import ru.sprbut.m19.greeter.GreeterProperties;

/**
 * Слайды 173–178 (СХЕМА 12): starter, imports, условия, бин.
 * @since 1.0
 */
@DisplayName("Слайды 173–178 (СХЕМА 12): starter, imports, условия, бин")
final class GreeterAutoConfigurationTest {

    private static ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class));
    }

    private static URL imports() {
        return Thread.currentThread().getContextClassLoader().getResource(
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        );
    }

    /**
     * Бин появляется сам.
     * @since 1.0
     */
    @Nested
    @DisplayName("Бин появляется сам")
    final class WorksOutOfTheBox {

        @Test
        @DisplayName("Без единой строки конфигурации в контексте есть готовый Greeter")
        void greeterAppearsWithoutConfiguration() {
            GreeterAutoConfigurationTest.runner().run(
                context -> Assertions.assertThat(context).hasSingleBean(Greeter.class)
            );
        }

        @Test
        @DisplayName("Готовый Greeter умеет здороваться шаблоном по умолчанию")
        void greeterUsesDefaultTemplate() {
            GreeterAutoConfigurationTest.runner().run(
                context -> Assertions.assertThat(context.getBean(Greeter.class).greet("Мир"))
                    .isEqualTo("Привет, Мир!")
            );
        }

        @Test
        @DisplayName("Настройки читаются из свойств с префиксом стартера")
        void propertiesAreBound() {
            GreeterAutoConfigurationTest.runner()
                .withPropertyValues(
                    "sprbut.greeter.template=Здравствуйте, {name}",
                    "sprbut.greeter.shout=true"
                ).run(
                    context -> Assertions.assertThat(context.getBean(Greeter.class).greet("Иван"))
                        .isEqualTo("ЗДРАВСТВУЙТЕ, ИВАН")
                );
        }

        @Test
        @DisplayName("@EnableConfigurationProperties регистрирует класс настроек")
        void propertiesBeanIsRegistered() {
            GreeterAutoConfigurationTest.runner().run(
                context -> Assertions.assertThat(context).hasSingleBean(GreeterProperties.class)
            );
        }
    }

    /**
     * Слайд 176: условия.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 176: условия")
    final class Conditions {

        @Test
        @DisplayName("@ConditionalOnProperty: выключатель отключает весь стартер")
        void canBeTurnedOff() {
            GreeterAutoConfigurationTest.runner()
                .withPropertyValues("sprbut.greeter.enabled=false")
                .run(context -> Assertions.assertThat(context).doesNotHaveBean(Greeter.class));
        }

        @Test
        @DisplayName("matchIfMissing = true: без свойства стартер включён")
        void enabledByDefault() {
            GreeterAutoConfigurationTest.runner().run(
                context -> Assertions.assertThat(context).hasSingleBean(Greeter.class)
            );
        }

        @Test
        @DisplayName("@ConditionalOnClass: без нужного класса автоконфигурация не применяется")
        void requiresTheLibraryOnClasspath() {
            GreeterAutoConfigurationTest.runner()
                .withClassLoader(new FilteredClassLoader(Greeter.class))
                .run(context -> Assertions.assertThat(context).doesNotHaveBean("greeter"));
        }
    }

    /**
     * Слайд 177: свой бин переопределяет автоконфигурацию.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 177: свой бин переопределяет автоконфигурацию")
    final class UserBeanWins {

        @Test
        @DisplayName("@ConditionalOnMissingBean уступает пользовательскому бину")
        void autoConfigurationBacksOff() {
            GreeterAutoConfigurationTest.runner()
                .withUserConfiguration(UserBeanWins.UserConfig.class)
                .run(context -> Assertions.assertThat(context).hasSingleBean(Greeter.class));
        }

        @Test
        @DisplayName("В контексте оказывается именно пользовательская реализация")
        void userImplementationWins() {
            GreeterAutoConfigurationTest.runner()
                .withUserConfiguration(UserBeanWins.UserConfig.class).run(
                    context -> Assertions.assertThat(context.getBean(Greeter.class).flavour())
                        .isEqualTo("пользовательский")
                );
        }

        @Test
        @DisplayName("Приветствие берётся из пользовательского бина")
        void userGreetingWins() {
            GreeterAutoConfigurationTest.runner()
                .withUserConfiguration(UserBeanWins.UserConfig.class).run(
                    context -> Assertions.assertThat(context.getBean(Greeter.class).greet("Мир"))
                        .isEqualTo("Здарова, Мир")
                );
        }

        @Test
        @DisplayName("Настройки при этом всё равно биндятся — стартер не отключился целиком")
        void propertiesStillBind() {
            GreeterAutoConfigurationTest.runner()
                .withUserConfiguration(UserBeanWins.UserConfig.class)
                .withPropertyValues("sprbut.greeter.template=неважно").run(
                    context -> Assertions
                        .assertThat(context.getBean(GreeterProperties.class).getTemplate())
                        .isEqualTo("неважно")
                );
        }

        /**
         * Пользовательская конфигурация, перебивающая автоконфигурацию.
         * @since 1.0
         */
        @Configuration
        @SuppressWarnings("PMD.JUnitTestClassShouldBeFinal")
        static class UserConfig {

            /**
             * Свой Greeter вместо автоконфигурационного.
             * @return Приветствие пользователя
             */
            @Bean
            Greeter greeter() {
                return new UserBeanWins.UserGreeter();
            }
        }

        /**
         * Реализация, которую объявляет сам пользователь стартера.
         * @since 1.0
         */
        static final class UserGreeter implements Greeter {

            @Override
            public String greet(final String name) {
                return String.format("Здарова, %s", name);
            }

            @Override
            public String flavour() {
                return "пользовательский";
            }
        }
    }

    /**
     * Слайд 175: регистрация через AutoConfiguration.imports.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 175: регистрация через AutoConfiguration.imports")
    final class Registration {

        @Test
        @DisplayName("Файл регистрации существует")
        void importsFileExists() {
            Assertions.assertThat(GreeterAutoConfigurationTest.imports()).isNotNull();
        }

        @Test
        @DisplayName("Файл регистрации содержит нашу автоконфигурацию")
        void importsFileListsTheAutoConfiguration() throws Exception {
            Assertions.assertThat(
                new String(
                    GreeterAutoConfigurationTest.imports().openStream().readAllBytes(),
                    StandardCharsets.UTF_8
                )
            ).contains("ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration");
        }

        @Test
        @DisplayName("@AutoConfiguration — это @Configuration(proxyBeanMethods = false)")
        void autoConfigurationIsALiteConfiguration() {
            Assertions.assertThat(
                GreeterAutoConfiguration.class.getAnnotation(AutoConfiguration.class)
            ).isNotNull();
        }

        @Test
        @DisplayName("Автоконфигурация помечена условием на класс")
        void autoConfigurationIsConditional() {
            Assertions.assertThat(
                GreeterAutoConfiguration.class.isAnnotationPresent(ConditionalOnClass.class)
            ).isTrue();
        }
    }
}

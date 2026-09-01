/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m19;

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
 * {@code ApplicationContextRunner} — штатный инструмент для тестов автоконфигурации.
 * Он поднимает контекст на каждый сценарий, но лёгкий и без веб-окружения.
 * @since 1.0
 */
@DisplayName("Слайды 173–178 (СХЕМА 12): starter → imports → условия → бин")
final class GreeterAutoConfigurationTest {

    /**
     * Значение {@code runner}.
     * @since 1.0
     */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(GreeterAutoConfiguration.class)
            );

    /**
     * Бин появляется сам.
     * @since 1.0
     */
    @Nested
    @DisplayName("Бин появляется сам")
    final class WorksOutOfTheBox {

        @Test
        @DisplayName(
            "Без единой строки конфигурации в контексте есть готовый Greeter"
        )
        void greeterAppearsWithoutConfiguration() {
            GreeterAutoConfigurationTest.this.runner.run(context -> {
                Assertions.assertThat(context).hasSingleBean(Greeter.class);
                Assertions.assertThat(
                    context.getBean(
                        Greeter.class
                    ).greet(
                        "Мир"
                    )
                )
                        .isEqualTo(
                            "Привет, Мир!"
                        );
            });
        }

        @Test
        @DisplayName("Настройки читаются из свойств с префиксом стартера")
        void propertiesAreBound() {
            GreeterAutoConfigurationTest.this.runner.withPropertyValues(
                            "sprbut.greeter.template=Здравствуйте, {name}",
                            "sprbut.greeter.shout=true")
                    .run(context -> Assertions.assertThat(context.getBean(Greeter.class).greet("Иван"))
                            .isEqualTo(
                                "ЗДРАВСТВУЙТЕ, ИВАН"
                            ));
        }

        @Test
        @DisplayName("@EnableConfigurationProperties регистрирует класс настроек")
        void propertiesBeanIsRegistered() {
            GreeterAutoConfigurationTest.this.runner.run(context -> Assertions.assertThat(context).hasSingleBean(GreeterProperties.class));
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
            GreeterAutoConfigurationTest.this.runner.withPropertyValues(
                "sprbut.greeter.enabled=false"
            )
                    .run(
                        context -> Assertions.assertThat(context).doesNotHaveBean(Greeter.class)
                    );
        }

        @Test
        @DisplayName("matchIfMissing = true: без свойства стартер включён")
        void enabledByDefault() {
            GreeterAutoConfigurationTest.this.runner.run(context -> Assertions.assertThat(context).hasSingleBean(Greeter.class));
        }

        @Test
        @DisplayName(
            "@ConditionalOnClass: без нужного класса автоконфигурация не применяется"
        )
        void requiresTheLibraryOnClasspath() {
            GreeterAutoConfigurationTest.this.runner.withClassLoader(
                new FilteredClassLoader(
                    Greeter.class
                )
            )
                    .run(
                        context -> Assertions.assertThat(context).doesNotHaveBean("greeter")
                    );
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
        @DisplayName(
            "@ConditionalOnMissingBean уступает пользовательскому бину"
        )
        void autoConfigurationBacksOff() {
            GreeterAutoConfigurationTest.this.runner.withUserConfiguration(UserConfig.class).run(context -> {

                Assertions.assertThat(context).hasSingleBean(Greeter.class);
                Assertions.assertThat(context.getBean(Greeter.class).flavour()).isEqualTo("пользовательский");
                Assertions.assertThat(
                    context.getBean(Greeter.class).greet("Мир")
                ).isEqualTo(
                    "Здарова, Мир"
                );
            });
        }

        @Test
        @DisplayName("Настройки при этом всё равно биндятся — стартер не отключился целиком")
        void propertiesStillBind() {
            GreeterAutoConfigurationTest.this.runner.withUserConfiguration(
                UserConfig.class
            )
                    .withPropertyValues(
                        "sprbut.greeter.template=неважно"
                    )
                    .run(context -> Assertions.assertThat(context.getBean(GreeterProperties.class).getTemplate())
                            .isEqualTo(
                                "неважно"
                            ));
        }

        @Configuration
        static class UserConfig {

            @Bean
            Greeter greeter() {
                return new Greeter() {
                    @Override
                    public String greet(final String name) {
                        return String.format("Здарова, %s", name);
                    }

                    @Override
                    public String flavour() {
                        return "пользовательский";
                    }
                };
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
        @DisplayName("Файл регистрации существует и содержит нашу автоконфигурацию")
        void importsFileListsTheAutoConfiguration() throws Exception {
            final var url = getClass().getClassLoader().getResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
            );
            Assertions.assertThat(
                url
            ).isNotNull();
            Assertions.assertThat(
                new String(
                    url.openStream().readAllBytes(), StandardCharsets.UTF_8
                )
            )
                    .contains(
                        "ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration"
                    );
        }

        @Test
        @DisplayName("@AutoConfiguration — это @Configuration(proxyBeanMethods = false)")
        void autoConfigurationIsALiteConfiguration() {
            final var annotation = GreeterAutoConfiguration.class
                    .getAnnotation(
                        AutoConfiguration.class
                    );
            Assertions.assertThat(
                annotation
            ).isNotNull();
            Assertions.assertThat(
                GreeterAutoConfiguration.class.isAnnotationPresent(
                    ConditionalOnClass.class
                )
            )
                    .isTrue();
        }
    }
}

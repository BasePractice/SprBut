package ru.sprbut.m18;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Слайды 161, 170: {@code ApplicationContextInitializer}, {@code ApplicationRunner}
 * и {@code BeanFactoryPostProcessor} — три хука, которые вызываются между событиями.
 * <p>
 * У каждого своё место в последовательности и своя задача:
 * <ul>
 *   <li>{@code ApplicationContextInitializer} — контекст создан, но пуст.
 *       Здесь регистрируют источники настроек и профили программно;</li>
 *   <li>{@code BeanFactoryPostProcessor} — определения бинов загружены, сами бины
 *       ещё не созданы. Это <b>не</b> {@code BeanPostProcessor} из модуля 14:
 *       тот работает с готовыми объектами, а этот — с их описаниями;</li>
 *   <li>{@code ApplicationRunner} / {@code CommandLineRunner} — контекст поднят.
 *       Отсюда запускают разовые задачи при старте.</li>
 * </ul>
 */
public final class StartupHooks {

    private StartupHooks() {
    }

    /** Шаг 3: контекст создан, бинов нет. */
    public static class MarkerInitializer
            implements ApplicationContextInitializer<GenericApplicationContext> {

        @Override
        public void initialize(GenericApplicationContext context) {
            StartupLog.record("3-ApplicationContextInitializer");
            // Программное добавление источника настроек — типичная задача инициализатора
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("initializer",
                            Map.of("sprbut.startup.injected", "да")));
        }
    }

    /**
     * Шаг 6: работает с {@code BeanDefinition}, а не с объектами.
     * Бины ещё не созданы — их можно переопределить.
     */
    public static class DefinitionTweaker implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                throws BeansException {
            StartupLog.record("6-BeanFactoryPostProcessor:определений="
                    + beanFactory.getBeanDefinitionCount());
        }
    }

    /** Шаг 9а: раннер с разобранными аргументами. */
    @Order(1)
    public static class FirstRunner implements ApplicationRunner {

        @Override
        public void run(ApplicationArguments args) {
            StartupLog.record("9a-ApplicationRunner:опции=" + args.getOptionNames());
        }
    }

    /** Шаг 9б: раннер с сырым массивом аргументов. */
    @Order(2)
    public static class SecondRunner implements CommandLineRunner {

        @Override
        public void run(String... args) {
            StartupLog.record("9b-CommandLineRunner:аргументов=" + args.length);
        }
    }

    @Configuration
    public static class HooksConfig {

        @Bean
        public static BeanFactoryPostProcessor definitionTweaker() {
            return new DefinitionTweaker();
        }

        @Bean
        public FirstRunner firstRunner() {
            return new FirstRunner();
        }

        @Bean
        public SecondRunner secondRunner() {
            return new SecondRunner();
        }
    }
}

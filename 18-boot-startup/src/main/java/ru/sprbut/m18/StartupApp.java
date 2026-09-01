/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m18;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Слайды 156–157: {@code BootstrapContext} и {@code ApplicationContext}.
 *
 * <p>{@code SpringApplication.run} — это не «запустить main», а вполне конкретная
 * последовательность шагов. Здесь она запускается управляемо, чтобы её можно
 * было наблюдать и проверять в тестах.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public class StartupApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public StartupApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы
     */
    public static void main(final String[] args) {
        run(args).close();
    }

    /**
     * Запуск с ручной регистрацией ранних слушателей.
     *
     * <p>Слушателей {@code ApplicationStartingEvent} и
     * {@code ApplicationEnvironmentPreparedEvent} нельзя объявить бинами:
     * контекста в этот момент ещё не существует. Их регистрируют либо здесь,
     * либо в {@code META-INF/spring.factories}.</p>
     * @param args Аргументы
     * @return Запуск с ручной регистрацией ранних слушателей
     */
    public static ConfigurableApplicationContext run(final String... args) {
        final SpringApplication application = new SpringApplication(StartupApp.class);
        application.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        application.addListeners(
                new StartupListeners.Starting(),
                new StartupListeners.EnvironmentPrepared(),
                new StartupListeners.ContextInitialized(),
                new StartupListeners.Prepared(),
                new StartupListeners.Refreshed(),
                new StartupListeners.Started(),
                new StartupListeners.Ready(),
                new StartupListeners.Failed());
        application.addInitializers(new StartupHooks.MarkerInitializer());
        return application.run(args);
    }

    /**
     * Вариант запуска, который падает на создании бина — чтобы увидеть ApplicationFailedEvent.
     * @param args Аргументы
     */
    public static void runFailing(final String... args) {
        final SpringApplication application =
                new SpringApplication(ru.sprbut.failing.FailingConfig.class);
        application.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        application.addListeners(new StartupListeners.Ready(), new StartupListeners.Failed());
        application.run(args);
    }

}

package ru.sprbut.m18;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Слайды 156–157: {@code BootstrapContext} и {@code ApplicationContext}.
 * <p>
 * {@code SpringApplication.run} — это не «запустить main», а вполне конкретная
 * последовательность шагов. Здесь она запускается управляемо, чтобы её можно
 * было наблюдать и проверять в тестах.
 */
@SpringBootApplication
public class StartupApp {

    public static void main(String[] args) {
        run(args).close();
    }

    /**
     * Запуск с ручной регистрацией ранних слушателей.
     * <p>
     * Слушателей {@code ApplicationStartingEvent} и
     * {@code ApplicationEnvironmentPreparedEvent} нельзя объявить бинами:
     * контекста в этот момент ещё не существует. Их регистрируют либо здесь,
     * либо в {@code META-INF/spring.factories}.
     */
    public static ConfigurableApplicationContext run(String... args) {
        SpringApplication application = new SpringApplication(StartupApp.class);
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

    /** Вариант запуска, который падает на создании бина — чтобы увидеть ApplicationFailedEvent. */
    public static void runFailing(String... args) {
        SpringApplication application =
                new SpringApplication(ru.sprbut.failing.FailingConfig.class);
        application.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        application.addListeners(new StartupListeners.Ready(), new StartupListeners.Failed());
        application.run(args);
    }

}

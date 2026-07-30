package ru.sprbut.m16;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входа модуля. {@code @ConfigurationPropertiesScan} находит классы
 * с {@code @ConfigurationProperties} — без него их пришлось бы регистрировать
 * вручную через {@code @EnableConfigurationProperties}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigurationApp {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationApp.class, args);
    }
}

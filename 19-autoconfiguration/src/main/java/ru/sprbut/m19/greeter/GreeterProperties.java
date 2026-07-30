package ru.sprbut.m19.greeter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки стартера. Префикс — имя стартера, это соглашение всей экосистемы:
 * {@code spring.datasource.*}, {@code spring.jpa.*}, {@code sprbut.greeter.*}.
 * <p>
 * Модуль подключает {@code spring-boot-configuration-processor} — он собирает
 * из этого класса файл {@code META-INF/spring-configuration-metadata.json},
 * благодаря которому IDE подсказывает ключи в {@code application.yaml}.
 * Это ещё один annotation processor (модуль 07) в реальном деле.
 */
@ConfigurationProperties(prefix = "sprbut.greeter")
public class GreeterProperties {

    /** Шаблон приветствия; {@code {name}} подставляется. */
    private String template = "Привет, {name}!";

    /** Писать ли приветствие заглавными буквами. */
    private boolean shout;

    /** Включён ли стартер вообще. */
    private boolean enabled = true;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isShout() {
        return shout;
    }

    public void setShout(boolean shout) {
        this.shout = shout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

package ru.sprbut.m13.componentvsbean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Слайд 108: «{@code @Component} vs {@code @Bean}: свой класс или чужой».
 * <p>
 * Это единственный практический критерий выбора:
 * <ul>
 *   <li>класс <b>ваш</b> — вешайте {@code @Component} прямо на него, пусть его
 *       найдёт сканирование;</li>
 *   <li>класс <b>чужой</b> (из библиотеки) — аннотацию поставить некуда,
 *       остаётся {@code @Bean}-метод в конфигурации.</li>
 * </ul>
 * Технически они дают одно и то же: определение бина. Разница — где живёт
 * решение о его создании: рядом с классом или в отдельной конфигурации.
 */
public final class ComponentVsBean {

    private ComponentVsBean() {
    }

    /** Свой класс — можно пометить аннотацией. */
    @Component
    public static class OwnService {

        public String describe() {
            return "свой класс, найден сканированием";
        }
    }

    /**
     * Имитация класса из чужой библиотеки: аннотацию поставить некуда,
     * а конструктор ещё и требует параметр.
     */
    public static class ThirdPartyClient {

        private final String endpoint;
        private final int timeout;

        public ThirdPartyClient(String endpoint, int timeout) {
            this.endpoint = endpoint;
            this.timeout = timeout;
        }

        public String describe() {
            return "чужой класс: " + endpoint + " (" + timeout + " мс)";
        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = OwnService.class)
    public static class Config {

        /** Чужой класс регистрируется @Bean-методом — другого способа нет. */
        @Bean
        public ThirdPartyClient thirdPartyClient() {
            return new ThirdPartyClient("https://api.example.com", 3_000);
        }

        /** Ещё более чужой: класс из JDK, который надо настроить. */
        @Bean
        public TimeZone applicationTimeZone() {
            return TimeZone.getTimeZone(ZoneId.of("Europe/Moscow"));
        }
    }
}

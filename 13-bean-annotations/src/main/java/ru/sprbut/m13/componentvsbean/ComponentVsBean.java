/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m13.componentvsbean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Слайд 108: «{@code @Component} vs {@code @Bean}: свой класс или чужой».
 *
 * <p>Это единственный практический критерий выбора:
 * <ul>
 * <li>класс <b>ваш</b> — вешайте {@code @Component} прямо на него, пусть его
 * найдёт сканирование;</li>
 * <li>класс <b>чужой</b> (из библиотеки) — аннотацию поставить некуда,
 * остаётся {@code @Bean}-метод в конфигурации.</li>
 * </ul>
 * Технически они дают одно и то же: определение бина. Разница — где живёт
 * решение о его создании: рядом с классом или в отдельной конфигурации.</p>
 *
 * @since 1.0
 */
public final class ComponentVsBean {

    private ComponentVsBean() {
    }

    /**
     * Свой класс — можно пометить аннотацией.
     * @since 1.0
     */
    @Component
    public static class OwnService {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public OwnService() {
            // нечего инициализировать
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return "свой класс, найден сканированием";
        }
    }

    /**
     * Имитация класса из чужой библиотеки: аннотацию поставить некуда,
     * а конструктор ещё и требует параметр.
     * @since 1.0
     */
    public static class ThirdPartyClient {

        /**
         * Значение {@code endpoint}.
         */
        private final String endpoint;
        /**
         * Значение {@code timeout}.
         */

        private final int timeout;

        /**
         * Основной конструктор.
         * @param endpoint Значение {@code endpoint}
         * @param timeout Значение {@code timeout}
         */
        public ThirdPartyClient(final String endpoint, final int timeout) {
            this.endpoint = endpoint;
            this.timeout = timeout;
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return "чужой класс: " + this.endpoint + " (" + this.timeout + " мс)";
        }
    }

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    @ComponentScan(basePackageClasses = OwnService.class)
    public static class Config {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Config() {
            // нечего инициализировать
        }

        /**
         * Чужой класс регистрируется @Bean-методом — другого способа нет.
         * @return Чужой класс регистрируется @Bean-методом — другого способа нет
         */
        @Bean
        public ThirdPartyClient thirdPartyClient() {
            return new ThirdPartyClient("https://api.example.com", 3_000);
        }

        /**
         * Ещё более чужой: класс из JDK, который надо настроить.
         * @return Ещё более чужой: класс из JDK, который надо настроить
         */
        @Bean
        public TimeZone applicationTimeZone() {
            return TimeZone.getTimeZone(ZoneId.of("Europe/Moscow"));
        }
    }
}

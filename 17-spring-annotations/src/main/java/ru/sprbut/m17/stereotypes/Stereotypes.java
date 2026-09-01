/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m17.stereotypes;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Слайды 140–144: {@code @Component}, {@code @Repository}, {@code @Controller},
 * {@code @Service}.
 *
 * <p>Технически все четыре — одно и то же: {@code @Repository}, {@code @Service}
 * и {@code @Controller} помечены мета-аннотацией {@code @Component}, и сканер
 * находит их одинаково (модуль 06).</p>
 *
 * <p>Разница не в механике, а в двух других вещах:
 * <ul>
 * <li><b>смысл</b> — по стереотипу видно роль класса в слоистой архитектуре;</li>
 * <li><b>поведение</b> — на стереотип можно навесить обработку.
 * {@code @Repository} включает трансляцию исключений драйвера БД
 * в общую иерархию {@code DataAccessException}, а {@code @Controller}
 * делает класс видимым для {@code DispatcherServlet}.</li>
 * </ul></p>
 *
 * @since 1.0
 */
public final class Stereotypes {

    private Stereotypes() {
    }

    /**
     * Значение {@code PlainComponent}.
     * @since 1.0
     */
    @Component
    public static class PlainComponent {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public PlainComponent() {
            // нечего инициализировать
        }

        /**
         * Роль.
         * @return Роль
         */
        public String role() {
            return "component";
        }
    }

    /**
     * Сервис.
     * @since 1.0
     */
    @Service
    public static class BusinessService {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public BusinessService() {
            // нечего инициализировать
        }

        /**
         * Роль.
         * @return Роль
         */
        public String role() {
            return "service";
        }
    }

    /**
     * {@code @Repository} — единственный стереотип, который <b>меняет поведение</b>
     * без дополнительной настройки: включается
     * {@code PersistenceExceptionTranslationPostProcessor}.
     * @since 1.0
     */
    @Repository
    public static class DataRepository {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public DataRepository() {
            // нечего инициализировать
        }

        /**
         * Роль.
         * @return Роль
         */
        public String role() {
            return "repository";
        }
    }

    /**
     * Значение {@code WebController}.
     * @since 1.0
     */
    @Controller
    public static class WebController {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public WebController() {
            // нечего инициализировать
        }

        /**
         * Роль.
         * @return Роль
         */
        public String role() {
            return "controller";
        }
    }

    /**
     * Класс без стереотипа — сканер его не найдёт.
     * @since 1.0
     */
    public static class NotAComponent {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public NotAComponent() {
            // нечего инициализировать
        }

        /**
         * Роль.
         * @return Роль
         */
        public String role() {
            return "не бин";
        }
    }
}

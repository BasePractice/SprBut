/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.modules;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Слайд 120 (СХЕМА 8): «карта Spring: Core в основании, Boot, Data, Security сверху».
 *
 * <p>Карта, оформленная как данные — чтобы утверждения о зависимостях между
 * модулями можно было проверить, а не запоминать.</p>
 *
 * <p>Главное, что из неё следует: <b>Spring Core ни от чего не зависит</b>,
 * а всё остальное — прямо или косвенно от него. Именно поэтому «выучить Spring»
 * начинают с контейнера, а не с веба.</p>
 *
 * @param name Имя модуля
 * @param layer Слой на карте
 * @param responsibility За что модуль отвечает
 * @param dependsOn Модули, от которых он зависит напрямую
 * @since 1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public record SpringModuleMap(
    String name, Layer layer, String responsibility, List<String> dependsOn
) {

    /**
     * Модуль spring-core.
     */
    public static final SpringModuleMap CORE = new SpringModuleMap(
        "spring-core",
        Layer.FOUNDATION,
        "IoC-контейнер, DI, ресурсы, конвертация типов",
        List.of()
    );

    /**
     * Модуль spring-beans.
     */
    public static final SpringModuleMap BEANS = new SpringModuleMap(
        "spring-beans",
        Layer.FOUNDATION,
        "BeanFactory, определения бинов, BeanPostProcessor",
        List.of("spring-core")
    );

    /**
     * Модуль spring-context.
     */
    public static final SpringModuleMap CONTEXT = new SpringModuleMap(
        "spring-context",
        Layer.CORE,
        "ApplicationContext, события, аннотации конфигурации",
        List.of("spring-core", "spring-beans")
    );

    /**
     * Модуль spring-aop.
     */
    public static final SpringModuleMap AOP = new SpringModuleMap(
        "spring-aop",
        Layer.CORE,
        "прокси вокруг бинов, аспекты, перехват вызовов",
        List.of("spring-core", "spring-beans")
    );

    /**
     * Модуль spring-jdbc.
     */
    public static final SpringModuleMap JDBC = new SpringModuleMap(
        "spring-jdbc",
        Layer.DATA,
        "JdbcTemplate, перевод исключений драйвера в общую иерархию",
        List.of("spring-core", "spring-beans", "spring-tx")
    );

    /**
     * Модуль spring-tx.
     */
    public static final SpringModuleMap TX = new SpringModuleMap(
        "spring-tx",
        Layer.DATA,
        "декларативные транзакции; работает через AOP-прокси",
        List.of("spring-core", "spring-beans", "spring-aop")
    );

    /**
     * Модуль spring-webmvc.
     */
    public static final SpringModuleMap MVC = new SpringModuleMap(
        "spring-webmvc",
        Layer.WEB,
        "DispatcherServlet, контроллеры, маршрутизация запросов",
        List.of("spring-core", "spring-beans", "spring-context", "spring-web")
    );

    /**
     * Модуль spring-data.
     */
    public static final SpringModuleMap DATA = new SpringModuleMap(
        "spring-data",
        Layer.DATA,
        "репозитории по интерфейсам, генерация запросов по именам методов",
        List.of("spring-core", "spring-context", "spring-tx")
    );

    /**
     * Модуль spring-security.
     */
    public static final SpringModuleMap SECURITY = new SpringModuleMap(
        "spring-security",
        Layer.INFRASTRUCTURE,
        "аутентификация, авторизация, фильтры; @PreAuthorize — тоже через AOP",
        List.of("spring-core", "spring-context", "spring-aop")
    );

    /**
     * Модуль spring-boot.
     */
    public static final SpringModuleMap BOOT = new SpringModuleMap(
        "spring-boot",
        Layer.PLATFORM,
        "автоконфигурация, стартеры, встроенный сервер",
        List.of("spring-core", "spring-context")
    );

    /**
     * Модуль spring-cloud.
     */
    public static final SpringModuleMap CLOUD = new SpringModuleMap(
        "spring-cloud",
        Layer.PLATFORM,
        "распределённая конфигурация, service discovery, отказоустойчивость",
        List.of("spring-boot")
    );

    /**
     * Компактный конструктор: список зависимостей делается неизменяемым.
     */
    public SpringModuleMap {
        dependsOn = List.copyOf(dependsOn);
    }

    /**
     * Карта из имени модуля в сам модуль.
     * @return Карта из имени модуля в сам модуль
     */
    public static Map<String, SpringModuleMap> byName() {
        return SpringModuleMap.all()
            .stream()
            .collect(
                Collectors.toMap(
                    SpringModuleMap::name,
                    module -> module,
                    (first, second) -> first,
                    LinkedHashMap::new
                )
            );
    }

    /**
     * Модули, ни от чего не зависящие, — основание карты.
     * @return Модули, ни от чего не зависящие
     */
    public static List<String> foundation() {
        return SpringModuleMap.all()
            .stream()
            .filter(module -> module.dependsOn().isEmpty())
            .map(SpringModuleMap::name)
            .toList();
    }

    /**
     * Полный транзитивный набор зависимостей модуля.
     * @param module Имя модуля
     * @return Полный транзитивный набор зависимостей модуля
     */
    public static Set<String> transitiveDependencies(final String module) {
        final Set<String> found = new LinkedHashSet<>();
        SpringModuleMap.collect(module, found);
        found.remove(module);
        return found;
    }

    /**
     * Кто использует AOP — то есть у кого поведение реализовано через прокси.
     * @return Модули, поведение которых реализовано через прокси
     */
    public static List<String> builtOnAop() {
        return SpringModuleMap.all()
            .stream()
            .filter(
                module -> "spring-aop".equals(module.name())
                    || SpringModuleMap.transitiveDependencies(module.name())
                        .contains("spring-aop")
            )
            .map(SpringModuleMap::name)
            .toList();
    }

    /**
     * Все элементы.
     * @return Все элементы
     */
    public static List<SpringModuleMap> all() {
        return List.of(
            SpringModuleMap.CORE, SpringModuleMap.BEANS, SpringModuleMap.CONTEXT,
            SpringModuleMap.AOP, SpringModuleMap.TX, SpringModuleMap.JDBC,
            SpringModuleMap.DATA, SpringModuleMap.MVC, SpringModuleMap.SECURITY,
            SpringModuleMap.BOOT, SpringModuleMap.CLOUD
        );
    }

    private static void collect(final String name, final Set<String> sink) {
        final SpringModuleMap module = SpringModuleMap.byName().get(name);
        if (module != null && sink.add(name)) {
            module.dependsOn()
                .forEach(dependency -> SpringModuleMap.collect(dependency, sink));
        }
    }

    /**
     * Слой на карте: основание, ядро, надстройки.
     * @since 1.0
     */
    public enum Layer {

        /**
         * Основание: ни от чего не зависит.
         */
        FOUNDATION,

        /**
         * Ядро контейнера.
         */
        CORE,

        /**
         * Работа с данными.
         */
        DATA,

        /**
         * Веб-слой.
         */
        WEB,

        /**
         * Инфраструктура вокруг приложения.
         */
        INFRASTRUCTURE,

        /**
         * Платформа целиком.
         */
        PLATFORM
    }
}

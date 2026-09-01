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
  * @param dependsOn Параметр типа
  * @param layer Параметр типа
 * @since 1.0
 */
public record SpringModuleMap(String name, Layer layer, String responsibility, List<String> dependsOn) {

    /**
     * Значение {@code CORE}.
     */
    public static final SpringModuleMap CORE = new SpringModuleMap(
            "spring-core", Layer.FOUNDATION,
            "IoC-контейнер, DI, ресурсы, конвертация типов",
            List.of());

    /**
     * Значение {@code BEANS}.
     */
    public static final SpringModuleMap BEANS = new SpringModuleMap(
            "spring-beans", Layer.FOUNDATION,
            "BeanFactory, определения бинов, BeanPostProcessor",
            List.of(
                "spring-core"
            ));

    /**
     * Значение {@code CONTEXT}.
     */
    public static final SpringModuleMap CONTEXT = new SpringModuleMap(
            "spring-context", Layer.CORE,
            "ApplicationContext, события, аннотации конфигурации",
            List.of(
                "spring-core", "spring-beans"
            ));

    /**
     * Значение {@code AOP}.
     */
    public static final SpringModuleMap AOP = new SpringModuleMap(
            "spring-aop", Layer.CORE,
            "прокси вокруг бинов, аспекты, перехват вызовов",
            List.of(
                "spring-core", "spring-beans"
            ));

    /**
     * Значение {@code JDBC}.
     */
    public static final SpringModuleMap JDBC = new SpringModuleMap(
            "spring-jdbc", Layer.DATA,
            "JdbcTemplate, перевод исключений драйвера в общую иерархию",
            List.of(
                "spring-core", "spring-beans", "spring-tx"
            ));

    /**
     * Значение {@code TX}.
     */
    public static final SpringModuleMap TX = new SpringModuleMap(
            "spring-tx", Layer.DATA,
            "декларативные транзакции; работает через AOP-прокси",
            List.of(
                "spring-core", "spring-beans", "spring-aop"
            ));

    /**
     * Значение {@code MVC}.
     */
    public static final SpringModuleMap MVC = new SpringModuleMap(
            "spring-webmvc", Layer.WEB,
            "DispatcherServlet, контроллеры, маршрутизация запросов",
            List.of(
                "spring-core", "spring-beans", "spring-context", "spring-web"
            ));

    /**
     * Значение {@code DATA}.
     */
    public static final SpringModuleMap DATA = new SpringModuleMap(
            "spring-data", Layer.DATA,
            "репозитории по интерфейсам, генерация запросов по именам методов",
            List.of(
                "spring-core", "spring-context", "spring-tx"
            ));

    /**
     * Значение {@code SECURITY}.
     */
    public static final SpringModuleMap SECURITY = new SpringModuleMap(
            "spring-security", Layer.INFRASTRUCTURE,
            "аутентификация, авторизация, фильтры; @PreAuthorize — тоже через AOP",
            List.of(
                "spring-core", "spring-context", "spring-aop"
            ));

    /**
     * Значение {@code BOOT}.
     */
    public static final SpringModuleMap BOOT = new SpringModuleMap(
            "spring-boot", Layer.PLATFORM,
            "автоконфигурация, стартеры, встроенный сервер",
            List.of(
                "spring-core", "spring-context"
            ));

    /**
     * Значение {@code CLOUD}.
     */
    public static final SpringModuleMap CLOUD = new SpringModuleMap(
            "spring-cloud", Layer.PLATFORM,
            "распределённая конфигурация, service discovery, отказоустойчивость",
            List.of(
                "spring-boot"
            ));

    /**
     * Значение {@code SpringModuleMap}.
     */
    public SpringModuleMap {
        dependsOn = List.copyOf(dependsOn);
    }

    /**
     * Имя.
     * @return Имя
     */
    public static Map<String, SpringModuleMap> byName() {
        return all().stream().collect(Collectors.toMap(
                SpringModuleMap::name, m -> m, (
                    a, b
                ) -> a, LinkedHashMap::new));
    }

    /**
     * Модули, ни от чего не зависящие, — основание карты.
     */
    public static List<String> foundation() {
        return all().stream()
                .filter(
                    m -> m.dependsOn().isEmpty()
                )
                .map(SpringModuleMap::name)
                .toList();
    }

    /**
     * Полный транзитивный набор зависимостей модуля.
     */
    public static Set<String> transitiveDependencies(final String moduleName) {
        final Set<String> result = new LinkedHashSet<>();
        collect(moduleName, result);
        result.remove(moduleName);
        return result;
    }

    /**
     * Кто использует AOP — то есть у кого поведение реализовано через прокси.
     */
    public static List<String> builtOnAop() {
        return all().stream()
                .filter(m -> m.name().equals("spring-aop")
                        || transitiveDependencies(
                            m.name()
                        ).contains(
                            "spring-aop"
                        ))
                .map(SpringModuleMap::name)
                .toList();
    }

    /**
     * Все элементы.
     * @return Все элементы
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public static List<SpringModuleMap> all() {
        return List.of(CORE, BEANS, CONTEXT, AOP, TX, JDBC, DATA, MVC, SECURITY, BOOT, CLOUD);
    }

    private static void collect(final String moduleName, final Set<String> sink) {
        final SpringModuleMap module = byName().get(moduleName);
        if (module == null || !sink.add(moduleName)) {
            return;
        }
        module.dependsOn().forEach(dependency -> collect(dependency, sink));
    }

    /**
     * Слой на карте: основание, ядро, надстройки.
     */
    public enum Layer { FOUNDATION, CORE, DATA, WEB, INFRASTRUCTURE, PLATFORM }
}

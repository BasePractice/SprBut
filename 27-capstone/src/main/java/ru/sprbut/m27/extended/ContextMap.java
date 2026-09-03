/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.extended;

import jakarta.servlet.Filter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.sprbut.m27.audit.Audited;

/**
 * <b>Расширенный итог курса.</b>
 *
 * <p>Приложение, которое рассказывает о себе само: карта живого контейнера,
 * построенная рефлексией по нему же.</p>
 *
 * <p>В одном классе сходится всё, чему был посвящён курс. Рефлексия читает
 * настоящие классы бинов и ищет в них аннотации (модули 01–06). Аннотация
 * {@link Audited} оказывается всего лишь меткой, которую кто-то должен прочесть
 * (модуль 05). Контейнер отдаёт определения бинов и их области видимости
 * (модули 11–14). {@code AopProxyUtils} снимает обёртку и показывает, что бин
 * в контексте — не тот объект, который написан в исходниках (модуль 15).</p>
 *
 * <p>Карта не ограничивается бинами. Таблица маршрутов — это прочитанные
 * аннотации {@code @GetMapping} и {@code @PostMapping} (модуль 20), цепочка
 * фильтров — то, во что превратилась конфигурация защиты (модуль 22).
 * И там и там аннотации давно прочитаны, а работает результат чтения.</p>
 *
 * <p>Практический смысл тот же, что у {@code /actuator/beans} и
 * {@code /actuator/mappings}: когда поведение приложения расходится с кодом,
 * разница почти всегда объясняется одной из строчек этой карты. «Запрос ушёл
 * не туда» — вопрос к маршрутам, «почему 401 вместо 403» — к порядку фильтров.</p>
 *
 * @since 1.0
 */
@Component
public final class ContextMap {

    /**
     * Контекст.
     */
    private final ConfigurableApplicationContext context;

    /**
     * Основной конструктор.
     * @param context Контекст
     */
    public ContextMap(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Карточки прикладных бинов — только своих, без инфраструктуры Spring.
     * @return Карточки прикладных бинов — только своих, без инфраструктуры Spring
     */
    public List<BeanCard> cards() {
        final ConfigurableListableBeanFactory beans = this.context.getBeanFactory();
        return Arrays.stream(beans.getBeanDefinitionNames())
            .filter(name -> ContextMap.mine(beans.getBeanDefinition(name).getBeanClassName()))
            .map(name -> this.card(name, beans))
            .toList();
    }

    /**
     * Проксирован ли бин — то есть перехватываются ли вызовы его методов.
     * @param name Имя
     * @return Проксирован ли бин — то есть перехватываются ли вызовы его методов
     */
    public boolean proxied(final String name) {
        return AopUtils.isAopProxy(this.context.getBean(name));
    }

    /**
     * Вид прокси: JDK-прокси вокруг интерфейса или CGLIB-подкласс.
     * @param name Имя
     * @return Вид прокси: JDK-прокси вокруг интерфейса или CGLIB-подкласс
     */
    public String proxy(final String name) {
        final Object bean = this.context.getBean(name);
        final String kind;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            kind = "jdk";
        } else if (AopUtils.isCglibProxy(bean)) {
            kind = "cglib";
        } else {
            kind = "none";
        }
        return kind;
    }

    /**
     * Маршруты приложения — то, что просматривает {@code DispatcherServlet}.
     *
     * <p>Реестр берётся по имени, а не по типу: actuator заводит второй такой же
     * бин для своих эндпоинтов, и выбор по типу стал бы неоднозначным.</p>
     *
     * @return Маршруты приложения
     */
    public List<String> routes() {
        return this.context
            .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class)
            .getHandlerMethods().entrySet().stream()
            .filter(entry -> ContextMap.mine(entry.getValue().getBeanType().getName()))
            .map(ContextMap::route)
            .sorted()
            .toList();
    }

    /**
     * Фильтры защиты в том порядке, в каком их проходит запрос.
     * @return Фильтры защиты в том порядке, в каком их проходит запрос
     */
    public List<String> filters() {
        return this.context.getBean(FilterChainProxy.class)
            .getFilterChains().getFirst().getFilters().stream()
            .map(ContextMap::name)
            .toList();
    }

    private static String route(final Map.Entry<RequestMappingInfo, HandlerMethod> entry) {
        return String.format(
            "%s %s",
            String.join(
                ",",
                entry.getKey().getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .sorted()
                    .toList()
            ),
            String.join(
                ",",
                entry.getKey().getPathPatternsCondition().getPatternValues().stream()
                    .sorted()
                    .toList()
            )
        );
    }

    private static String name(final Filter filter) {
        return filter.getClass().getSimpleName();
    }

    private BeanCard card(final String name, final ConfigurableListableBeanFactory beans) {
        final Class<?> type = AopProxyUtils.ultimateTargetClass(this.context.getBean(name));
        final String scope = beans.getBeanDefinition(name).getScope();
        final String named;
        if (scope.isEmpty()) {
            named = "singleton";
        } else {
            named = scope;
        }
        return new BeanCard(name, type.getName(), named, ContextMap.audited(type));
    }

    private static List<String> audited(final Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Audited.class))
            .map(ContextMap::operation)
            .sorted()
            .toList();
    }

    private static String operation(final Method method) {
        final Audited audited = method.getAnnotation(Audited.class);
        final String name;
        if (audited.value().isBlank()) {
            name = method.getName();
        } else {
            name = audited.value();
        }
        return name;
    }

    private static boolean mine(final String type) {
        return type != null && type.startsWith("ru.sprbut.m27");
    }
}

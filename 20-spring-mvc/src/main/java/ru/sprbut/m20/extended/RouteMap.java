/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.extended;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * <b>Расширенный пример модуля.</b>
 *
 * <p>Таблица маршрутов, снятая с самого приложения. Ровно её
 * {@code DispatcherServlet} и просматривает на каждый запрос, выбирая, какой
 * метод вызвать: аннотации {@code @GetMapping} и {@code @PostMapping} к этому
 * моменту давно прочитаны, а результат чтения лежит в
 * {@code RequestMappingHandlerMapping}.</p>
 *
 * <p>Здесь курс сходится сам с собой: аннотация — это метаданные (модули
 * 05–06), кто-то должен их прочесть (модуль 01), контейнер отдаёт готовый
 * результат чтения (модули 11–14). Практический смысл тот же, что
 * у {@code /actuator/mappings}: когда запрос уходит «не туда», ответ виден
 * в этой таблице, а не в исходниках.</p>
 *
 * @since 1.0
 */
@Component
public final class RouteMap {

    /**
     * Реестр маршрутов, собранный контейнером при старте.
     */
    private final RequestMappingHandlerMapping mapping;

    /**
     * Основной конструктор.
     * @param mapping Реестр маршрутов
     */
    public RouteMap(final RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Все маршруты приложения, упорядоченные по шаблону пути.
     * @return Все маршруты приложения, упорядоченные по шаблону пути
     */
    public List<RouteCard> cards() {
        return this.mapping.getHandlerMethods().entrySet().stream()
            .map(RouteMap::card)
            .sorted(Comparator.comparing(card -> String.join(",", card.patterns())))
            .toList();
    }

    /**
     * Маршруты, ведущие в указанный класс контроллера.
     * @param type Класс контроллера
     * @return Маршруты, ведущие в указанный класс контроллера
     */
    public List<RouteCard> of(final Class<?> type) {
        return this.cards().stream()
            .filter(card -> card.handler().startsWith(type.getSimpleName()))
            .toList();
    }

    private static RouteCard card(final Map.Entry<RequestMappingInfo, HandlerMethod> entry) {
        return new RouteCard(
            entry.getKey().getMethodsCondition().getMethods().stream()
                .map(Enum::name)
                .sorted()
                .toList(),
            entry.getKey().getPathPatternsCondition().getPatternValues().stream()
                .sorted()
                .toList(),
            RouteMap.handler(entry.getValue())
        );
    }

    private static String handler(final HandlerMethod handler) {
        final Method method = handler.getMethod();
        return String.format(
            "%s.%s", method.getDeclaringClass().getSimpleName(), method.getName()
        );
    }
}

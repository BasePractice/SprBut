/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m22.extended;

import jakarta.servlet.Filter;
import java.util.List;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * <b>Расширенный пример модуля.</b>
 *
 * <p>Цепочка фильтров, выписанная по именам. Ровно через неё проходит каждый
 * запрос, прежде чем попасть в {@code DispatcherServlet}, и порядок фильтров
 * в этом списке — не деталь реализации, а сама логика защиты: сначала запрос
 * опознают, потом решают, что ему можно, и только потом пускают дальше.</p>
 *
 * <p>Практический смысл прямой. «Почему 401 вместо 403» и «почему мой фильтр
 * не сработал» — вопросы про порядок в этом списке, и отвечает на них он,
 * а не исходники конфигурации. Настройки, написанные в
 * {@code SecurityFilterChain}, к этому моменту уже превратились в набор
 * объектов — как аннотации маршрутов в модуле 20.</p>
 *
 * @since 1.0
 */
@Component
public final class FilterMap {

    /**
     * Точка входа всей защиты: один сервлетный фильтр, за которым цепочки.
     */
    private final FilterChainProxy proxy;

    /**
     * Основной конструктор.
     * @param proxy Точка входа всей защиты
     */
    public FilterMap(final FilterChainProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Все цепочки приложения в порядке проверки.
     * @return Все цепочки приложения в порядке проверки
     */
    public List<ChainCard> cards() {
        return this.proxy.getFilterChains().stream()
            .map(FilterMap::card)
            .toList();
    }

    /**
     * Имена фильтров первой цепочки — той, через которую идёт обычный запрос.
     * @return Имена фильтров первой цепочки
     */
    public List<String> filters() {
        return this.cards().getFirst().filters();
    }

    private static ChainCard card(final SecurityFilterChain chain) {
        return new ChainCard(
            chain.toString().replaceAll(".*RequestMatcher\\s*", ""),
            chain.getFilters().stream()
                .map(FilterMap::name)
                .toList()
        );
    }

    private static String name(final Filter filter) {
        return filter.getClass().getSimpleName();
    }
}

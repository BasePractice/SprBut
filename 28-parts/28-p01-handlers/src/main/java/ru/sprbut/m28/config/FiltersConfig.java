/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.sprbut.m28.filters.AuditFilter;
import ru.sprbut.m28.filters.LimitFilter;

/**
 * Фильтры, зарегистрированные вручную.
 *
 * <p>Фильтр-бин с {@code @Component} Boot регистрирует на все пути
 * подряд, включая {@code /error} и служебные адреса. Обёртка
 * {@code FilterRegistrationBean} даёт то, чего аннотация не умеет:
 * шаблоны путей, порядок и имя. Фильтр при этом создаётся обычным
 * {@code new} и бином не является, поэтому второй раз Boot его
 * не зарегистрирует.</p>
 *
 * <p>{@code ShallowEtagHeaderFilter} — фильтр, который меняет уже готовый
 * ответ. Он подменяет ответ кэширующей обёрткой, дожидается, пока
 * контроллер допишет тело, считает по нему {@code ETag} и, если клиент
 * прислал тот же {@code If-None-Match}, заменяет тело кодом 304. Работу
 * контроллера это не экономит, только трафик: поэтому «неглубокий».</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class FiltersConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public FiltersConfig() {
        // нечего инициализировать
    }

    /**
     * Журнал тел запросов, только для путей API.
     * @return Регистрация фильтра
     */
    @Bean
    public FilterRegistrationBean<AuditFilter> audit() {
        final FilterRegistrationBean<AuditFilter> registration = new FilterRegistrationBean<>(
            new AuditFilter(LoggerFactory.getLogger(AuditFilter.class)::info, 1024)
        );
        registration.addUrlPatterns("/api/*");
        return registration;
    }

    /**
     * Ограничение длины тела: раньше всех, пока тело никто не прочитал.
     * @param resolver Обработчик исключений диспетчера
     * @return Регистрация фильтра
     */
    @Bean
    public FilterRegistrationBean<LimitFilter> limit(
        @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver
    ) {
        final FilterRegistrationBean<LimitFilter> registration =
            new FilterRegistrationBean<>(new LimitFilter(resolver, 1024));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * {@code ETag} для приветствия.
     * @return Регистрация фильтра
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etag() {
        final FilterRegistrationBean<ShallowEtagHeaderFilter> registration =
            new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/greeting");
        return registration;
    }
}

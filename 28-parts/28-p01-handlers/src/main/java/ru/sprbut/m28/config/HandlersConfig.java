/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sprbut.m28.converters.LenientEnumFactory;
import ru.sprbut.m28.errors.UnsupportedResolver;
import ru.sprbut.m28.formatters.PhoneFormatterFactory;
import ru.sprbut.m28.handlers.CurrentUserArgumentResolver;
import ru.sprbut.m28.interceptors.MdcPropagation;
import ru.sprbut.m28.interceptors.TimingInterceptor;
import ru.sprbut.m28.messages.UserCsvConverter;
import ru.sprbut.m28.returns.LinesReturnValueHandler;

/**
 * Регистратор участников, которых контейнер не найдёт сам.
 *
 * <p>Асимметрия, из-за которой этот класс существует: фильтр и конвертер
 * достаточно объявить бинами — их подхватят контейнер сервлетов и
 * автоконфигурация MVC. Интерсептор и резолвер аргумента бинами быть могут,
 * но диспетчер о них так и не узнает: свои списки он собирает из
 * {@code WebMvcConfigurer}, а не сканированием контекста.</p>
 *
 * <p>Поэтому все они создаются здесь обычным {@code new}: один способ
 * регистрации лучше двух, из которых работает только один. Методы класса
 * читаются как перечень мест, куда в Spring MVC можно встать со своим
 * кодом: интерсепторы, форматтеры, версии API, аргументы и результаты
 * методов, конвертеры сообщений, исключения, асинхронная обработка.</p>
 *
 * <p>У половины методов есть близнец, который выглядит так же, но
 * заменяет штатный список вместо того, чтобы дополнить его:
 * {@code configureHandlerExceptionResolvers} рядом с
 * {@code extendHandlerExceptionResolvers}, {@code disableDefaults} у
 * конвертеров сообщений. Здесь везде выбрано дополнение.</p>
 *
 * <p>{@code proxyBeanMethods = false} стоит не для скорости: с
 * умолчанием {@code true} контейнер расширяет класс конфигурации через
 * CGLIB, чтобы перехватывать вызовы {@code @Bean}-методов, а {@code final}
 * класс расширить нельзя, и контекст просто не поднимется. Перехватывать
 * здесь нечего — {@code @Bean}-методов в классе нет.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class HandlersConfig implements WebMvcConfigurer {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public HandlersConfig() {
        // нечего инициализировать
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new TimingInterceptor()).addPathPatterns("/api/**");
    }

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new PhoneFormatterFactory());
        registry.addConverterFactory(new LenientEnumFactory());
    }

    @Override
    public void configureApiVersioning(final ApiVersionConfigurer configurer) {
        configurer.useRequestHeader("X-API-Version")
            .setVersionRequired(false)
            .setDefaultVersion("1");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(final List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(new LinesReturnValueHandler());
    }

    @Override
    public void configureMessageConverters(final HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConvertersList(list -> list.add(new UserCsvConverter()));
    }

    @Override
    public void extendHandlerExceptionResolvers(final List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new UnsupportedResolver());
    }

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.registerCallableInterceptors(new MdcPropagation());
    }
}

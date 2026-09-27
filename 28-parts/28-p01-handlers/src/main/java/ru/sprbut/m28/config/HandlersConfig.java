/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sprbut.m28.handlers.CurrentUserArgumentResolver;
import ru.sprbut.m28.interceptors.TimingInterceptor;

/**
 * Регистратор участников, которых контейнер не найдёт сам.
 *
 * <p>Асимметрия, из-за которой этот класс существует: фильтр и конвертер
 * достаточно объявить бинами — их подхватят контейнер сервлетов и
 * автоконфигурация MVC. Интерсептор и резолвер аргумента бинами быть могут,
 * но диспетчер о них так и не узнает: свои списки он собирает из
 * {@code WebMvcConfigurer}, а не сканированием контекста.</p>
 *
 * <p>Поэтому оба создаются здесь обычным {@code new}: один способ
 * регистрации лучше двух, из которых работает только один.</p>
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
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}

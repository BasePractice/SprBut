/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15.aop;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Включает автоматическое создание прокси вокруг бинов, попадающих под pointcut.
 *
 * <p>{@code @EnableAspectJAutoProxy} регистрирует
 * {@code AnnotationAwareAspectJAutoProxyCreator} — обычный
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}, который
 * на шаге 6 жизненного цикла (модуль 14) возвращает вместо бина его прокси.</p>
 *
 * @since 1.0
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackageClasses = AopConfig.class)
public class AopConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AopConfig() {
        // нечего инициализировать
    }

}

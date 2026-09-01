/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15.extended;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * {@code exposeProxy = true} кладёт текущий прокси в ThreadLocal, чтобы его
 * можно было достать через {@code AopContext.currentProxy()}.
 *
 * <p>По умолчанию флаг выключен — и это правильно: обход self-invocation через
 * {@code AopContext} привязывает код к Spring и прячет проблему вместо того,
 * чтобы её решить.</p>
 *
 * @since 1.0
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@ComponentScan(basePackageClasses = ExtendedAopConfig.class)
public class ExtendedAopConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ExtendedAopConfig() {
        // нечего инициализировать
    }
}

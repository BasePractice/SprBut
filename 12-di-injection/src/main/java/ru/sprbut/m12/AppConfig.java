/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m12.domain.TaxService;
import ru.sprbut.m12.injection.ConstructorInjected;
import ru.sprbut.m12.jakarta.JakartaInjected;
import ru.sprbut.m12.locator.ServiceLocatorDemo;

/**
 * Конфигурация модуля: сканирует пакеты с примерами внедрения.
 *
 * <p>Классы с циклическими зависимостями лежат отдельно и <b>не</b> попадают
 * в сканирование — иначе контекст бы просто не поднялся.</p>
 *
 * @since 1.0
 */
@Configuration
@ComponentScan(
    basePackageClasses = {
        TaxService.class,
        ConstructorInjected.class,
        ServiceLocatorDemo.class,
        JakartaInjected.class
    }
)
public class AppConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AppConfig() {
        // нечего инициализировать
    }
}

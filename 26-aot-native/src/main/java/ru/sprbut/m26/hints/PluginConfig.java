/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m26.hints;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import ru.sprbut.m26.reflection.CsvPlugin;
import ru.sprbut.m26.reflection.Plugin;
import ru.sprbut.m26.reflection.PluginByName;

/**
 * Конфигурация, которая честно объявляет свою рефлексию.
 *
 * <p>{@code @ImportRuntimeHints} — единственная строчка разницы между приложением,
 * которое соберётся в native image, и приложением, которое соберётся,
 * запустится и упадёт на первом же плагине.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(PluginHints.class)
public final class PluginConfig {

    /**
     * Открытый конструктор: конфигурацию создаёт контейнер.
     */
    public PluginConfig() {
        // нечего инициализировать
    }

    /**
     * Расширение, поднятое рефлексией по имени класса.
     * Метод фабрики бина не может быть статическим: тема модуля — обычный
     * {@code @Bean}, а статическая фабрика меняет порядок обработки конфигурации.
     * @return Расширение
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    @Bean
    public Plugin plugin() {
        return new PluginByName(CsvPlugin.class.getName()).plugin();
    }
}

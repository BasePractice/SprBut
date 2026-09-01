/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m23.autoconfigure;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Автоконфигурация часов приложения.
 *
 * <p>Ровно тот приём, что описан на слайде «Автоконфигурация»: разумное значение
 * по умолчанию, которое молча отступает, если у приложения есть своё.</p>
 *
 * <p>Почему это не может быть обычным {@code @Configuration}: условие
 * {@code @ConditionalOnMissingBean} смотрит на бины, зарегистрированные
 * <b>к моменту проверки</b>. Обычные конфигурации разбираются в произвольном
 * порядке, и условие выстрелило бы раньше, чем появился пользовательский бин, —
 * вместо подмены получилось бы падение с {@code BeanDefinitionOverrideException}.
 * Автоконфигурации Spring намеренно обрабатывает последними, и только поэтому
 * приём работает.</p>
 *
 * <p>Класс подключается не сканированием, а строкой в
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.</p>
 *
 * @since 1.0
 */
@AutoConfiguration
public class ClockAutoConfiguration {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ClockAutoConfiguration() {
        // нечего инициализировать
    }

    /**
     * Системные часы UTC — если приложение не принесло свои.
     * @return Системные часы UTC — если приложение не принесло свои
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

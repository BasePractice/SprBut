/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.failing;

import org.springframework.context.annotation.Bean;

/**
 * Конфигурация, которая намеренно роняет создание бина — чтобы увидеть
 * {@code ApplicationFailedEvent} вместо {@code ApplicationReadyEvent}.
 *
 * <p>Лежит <b>вне</b> пакета {@code ru.sprbut.m18} специально. Внутри него её
 * подхватило бы сканирование {@code @SpringBootApplication}: класс с
 * {@code @Bean}-методами считается «lite»-конфигурацией, даже если на нём нет
 * ни одного стереотипа. Тогда падал бы каждый запуск, а не только тот, где это нужно.</p>
 *
 * @since 1.0
 */
public class FailingConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public FailingConfig() {
        // нечего инициализировать
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    public String brokenBean() {
        throw new IllegalStateException("бин не создался");
    }
}

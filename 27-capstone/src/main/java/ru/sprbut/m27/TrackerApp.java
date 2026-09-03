/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// класс приложения обязан быть инстанцируемым: контейнер видит в нём
// конфигурацию, а не утилиту с одним main
// @checkstyle HideUtilityClassConstructorCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import ru.sprbut.m27.aot.TrackerHints;

/**
 * <b>Итоговое задание курса: SprBut Tracker.</b>
 *
 * <p>Небольшой трекер задач, собранный так, чтобы в нём встретилась каждая тема курса:
 * рефлексия и аннотации, свой AOP-аспект поверх собственной аннотации, внедрение
 * через конструктор, жизненный цикл бинов, внешняя конфигурация с профилями,
 * автоконфигурация, срезы тестов и подсказки для native image.</p>
 *
 * <p>{@code @SpringBootApplication} — это три аннотации в одной: {@code @Configuration},
 * {@code @ComponentScan} и {@code @EnableAutoConfiguration}. Первая делает класс
 * источником бинов, вторая находит компоненты в этом пакете и ниже,
 * третья подключает всё, что Boot сумеет вывести из classpath.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@ImportRuntimeHints(TrackerHints.class)
public final class TrackerApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TrackerApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы
     */
    public static void main(final String[] args) {
        SpringApplication.run(TrackerApp.class, args);
    }
}

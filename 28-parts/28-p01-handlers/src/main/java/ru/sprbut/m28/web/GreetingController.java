/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Два метода на одном пути, различённые версией API.
 *
 * <p>Версия — ещё одно условие выбора обработчика, наравне с путём и
 * методом HTTP. Начиная со Spring 7 его не нужно писать самому:
 * {@code HandlersConfig.configureApiVersioning} объясняет, где искать
 * версию в запросе, а атрибут {@code version} у {@code @GetMapping}
 * говорит, какую версию обслуживает метод.</p>
 *
 * <p>Запрос без версии получает версию по умолчанию, запрос с версией,
 * которую не обслуживает ни один метод, — ответ 400. Методы без
 * атрибута {@code version}, то есть все остальные контроллеры, подходят
 * к любой версии.</p>
 *
 * <p>На этот путь поставлен {@code ShallowEtagHeaderFilter}: приветствие
 * не меняется, и повторный запрос с тем же {@code ETag} получает 304.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/greeting")
public final class GreetingController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public GreetingController() {
        // нечего инициализировать
    }

    /**
     * Приветствие первой версии.
     * @return Приветствие
     */
    @GetMapping(version = "1")
    public String first() {
        return "Привет";
    }

    /**
     * Приветствие второй версии.
     * @return Приветствие
     */
    @GetMapping(version = "2")
    public String second() {
        return "Здравствуйте";
    }
}

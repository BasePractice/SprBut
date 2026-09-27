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
import ru.sprbut.m28.routing.Client;

/**
 * Два метода на одном пути, различённые своим условием.
 *
 * <p>Для диспетчера оба метода обслуживают {@code GET /api/device}.
 * Различает их {@code ClientCondition}: метод с меткой {@link Client}
 * подходит только запросу с заголовком {@code X-Client: mobile} и
 * при совпадении побеждает метод без условия.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/device")
public final class DeviceController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public DeviceController() {
        // нечего инициализировать
    }

    /**
     * Ответ для всех.
     * @return Вид клиента
     */
    @GetMapping
    public String common() {
        return "обычный клиент";
    }

    /**
     * Ответ для мобильного клиента.
     * @return Вид клиента
     */
    @Client("mobile")
    @GetMapping
    public String mobile() {
        return "мобильный клиент";
    }
}

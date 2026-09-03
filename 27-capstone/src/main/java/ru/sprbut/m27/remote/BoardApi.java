/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.remote;

import org.springframework.web.service.annotation.PostExchange;

/**
 * Доска соседнего сервиса, куда трекер сообщает о закрытых задачах.
 *
 * <p>Реализации у этого интерфейса нет и не будет: её собирает
 * {@code HttpServiceProxyFactory} динамическим прокси — тем самым средством,
 * которое в модуле 04 разбиралось на игрушечном примере. Курс замыкается:
 * то, с чего он начинался, здесь делает настоящую работу.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface BoardApi {

    /**
     * Отмечает задачу на доске и возвращает ответ соседа.
     * @param title Название задачи
     * @return Ответ соседа
     */
    @PostExchange("/notices")
    String notice(String title);
}

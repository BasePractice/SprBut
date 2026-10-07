/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сессии учёток: цепочки refresh-токенов, которые можно обменивать и отзывать.
 *
 * <p>Access-токен несёт идентификатор сессии; пока сессия активна,
 * токен действителен.</p>
 *
 * @since 1.0
 */
public interface Sessions {

    /**
     * Открыть сессию.
     * @param account Идентификатор учётки
     * @return Сессия с первым refresh-токеном
     */
    Ticket open(UUID account);

    /**
     * Обменять refresh-токен на новый в той же сессии.
     *
     * <p>Повторное использование уже обменянного токена закрывает сессию.</p>
     *
     * @param refresh Refresh-токен
     * @return Сессия с новым refresh-токеном, если обмен разрешён
     */
    Optional<Ticket> rotate(String refresh);

    /**
     * Активна ли сессия.
     * @param session Идентификатор сессии
     * @return Истина, если сессия не закрыта и не истекла
     */
    boolean active(UUID session);

    /**
     * Закрыть сессию refresh-токена, если она принадлежит учётке.
     * @param account Идентификатор учётки
     * @param refresh Refresh-токен
     */
    void close(UUID account, String refresh);

    /**
     * Закрыть все сессии учётки.
     * @param account Идентификатор учётки
     */
    void revoke(UUID account);

    /**
     * Активные сессии учётки, без токенов.
     * @param account Идентификатор учётки
     * @return Сессии: идентификатор, создание, истечение
     */
    List<Map<String, Object>> list(UUID account);
}

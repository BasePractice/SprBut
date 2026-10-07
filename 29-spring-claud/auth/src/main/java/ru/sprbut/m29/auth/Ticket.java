/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.UUID;

/**
 * Открытая или продлённая сессия: чья она и какой refresh-токен к ней выдан.
 * @param account Идентификатор учётки
 * @param session Идентификатор сессии
 * @param refresh Новый refresh-токен
 * @since 1.0
 */
public record Ticket(UUID account, UUID session, String refresh) {
}

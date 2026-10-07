/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import ru.sprbut.m29.identity.Identity;

/**
 * Учётки пользователей в сервисе auth.
 * @since 1.0
 */
@FunctionalInterface
public interface Accounts {

    /**
     * Удалить учётку вместе с её токенами.
     * @param caller Пользователь, от имени которого идёт удаление
     * @param id     Идентификатор учётки
     */
    void delete(Identity caller, UUID id);
}

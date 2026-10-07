/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ru.sprbut.m29.identity.Identity;

/**
 * Учётки в памяти для тестов: помнят, чьи учётки удалены.
 * @since 1.0
 */
final class FakeAccounts implements Accounts {

    /**
     * Удалённые учётки.
     */
    private final Set<UUID> deleted;

    /**
     * Конструктор без удалённых учёток.
     */
    FakeAccounts() {
        this(ConcurrentHashMap.newKeySet());
    }

    /**
     * Конструктор.
     * @param deleted Удалённые учётки
     */
    FakeAccounts(final Set<UUID> deleted) {
        this.deleted = deleted;
    }

    @Override
    public void delete(final Identity caller, final UUID id) {
        this.deleted.add(id);
    }

    /**
     * Удалена ли учётка.
     * @param id Идентификатор
     * @return Истина, если удалена
     */
    boolean gone(final UUID id) {
        return this.deleted.contains(id);
    }
}

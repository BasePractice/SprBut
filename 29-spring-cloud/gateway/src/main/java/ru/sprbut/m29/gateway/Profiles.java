/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.util.Map;
import reactor.core.publisher.Mono;
import ru.sprbut.m29.identity.Identity;

/**
 * Профили в сервисе profile.
 * @since 1.0
 */
@FunctionalInterface
public interface Profiles {

    /**
     * Профиль пользователя, прочитанный от его имени.
     * @param owner Пользователь
     * @return Поля профиля
     */
    Mono<Map<String, Object>> profile(Identity owner);
}

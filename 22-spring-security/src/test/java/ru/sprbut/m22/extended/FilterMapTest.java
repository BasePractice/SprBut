/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m22.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Расширенный пример: приложение показывает собственную цепочку фильтров.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Расширенный пример: приложение показывает собственную цепочку фильтров")
final class FilterMapTest {

    /**
     * Карта фильтров.
     */
    @Autowired
    private FilterMap map;

    @Test
    @DisplayName("защита приложения — это цепочка, а не одна проверка")
    void showsWholeChain() {
        MatcherAssert.assertThat(
            "цепочка фильтров оказалась пустой",
            this.map.filters(),
            Matchers.not(Matchers.empty())
        );
    }

    @Test
    @DisplayName("аутентификацией занят отдельный фильтр, а не контроллер")
    void containsAuthenticationFilter() {
        MatcherAssert.assertThat(
            "в цепочке нет фильтра, который опознаёт пользователя",
            this.map.filters(),
            Matchers.hasItem("BasicAuthenticationFilter")
        );
    }

    @Test
    @DisplayName("решение о доступе принимает последний фильтр цепочки")
    void endsWithAuthorization() {
        MatcherAssert.assertThat(
            "проверка доступа стоит не в конце цепочки",
            this.map.filters().getLast(),
            Matchers.equalTo("AuthorizationFilter")
        );
    }

    @Test
    @DisplayName("опознание идёт раньше решения о доступе")
    void authenticatesBeforeAuthorizing() {
        MatcherAssert.assertThat(
            "порядок нарушен: доступ проверяется раньше, чем пользователь опознан",
            this.map.filters().indexOf("BasicAuthenticationFilter"),
            Matchers.lessThan(this.map.filters().indexOf("AuthorizationFilter"))
        );
    }
}

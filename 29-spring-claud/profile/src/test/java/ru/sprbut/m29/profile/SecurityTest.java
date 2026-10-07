/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Тесты правил доступа {@link Security}.
 * @since 1.0
 */
@WebMvcTest
@Import({Security.class, SecurityTest.Fakes.class})
final class SecurityTest {

    @Test
    @DisplayName("аноним не может прочитать профиль")
    void dontLetAnonymousReadProfile(
        @Autowired final MockMvc mvc,
        @Autowired final Profiles profiles
    ) throws Exception {
        MatcherAssert.assertThat(
            "аноним прочитал профиль",
            mvc.perform(
                MockMvcRequestBuilders.get(
                    "/profiles/{id}",
                    profiles.add(UUID.randomUUID(), UUID.randomUUID().toString(), "Яна").id()
                )
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(401)
        );
    }

    @Test
    @DisplayName("пользователь читает собственный профиль")
    void letsUserReadHimself(
        @Autowired final MockMvc mvc,
        @Autowired final Profiles profiles
    ) throws Exception {
        final UUID id = UUID.randomUUID();
        profiles.add(id, UUID.randomUUID().toString(), "Лев");
        MatcherAssert.assertThat(
            "пользователь не смог прочитать собственный профиль",
            mvc.perform(
                MockMvcRequestBuilders.get("/profiles/{id}", id)
                    .header("X-User-Id", id.toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "lev")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(200)
        );
    }

    @Test
    @DisplayName("пользователь не может прочитать чужой профиль")
    void dontLetUserReadAnotherProfile(
        @Autowired final MockMvc mvc,
        @Autowired final Profiles profiles
    ) throws Exception {
        MatcherAssert.assertThat(
            "пользователь прочитал чужой профиль",
            mvc.perform(
                MockMvcRequestBuilders.get(
                    "/profiles/{id}",
                    profiles.add(UUID.randomUUID(), UUID.randomUUID().toString(), "Глеб").id()
                ).header("X-User-Id", UUID.randomUUID().toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "vera")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("администратор читает профиль другого пользователя")
    void letsAdminReadAnotherProfile(
        @Autowired final MockMvc mvc,
        @Autowired final Profiles profiles
    ) throws Exception {
        MatcherAssert.assertThat(
            "администратор не смог прочитать профиль другого пользователя",
            mvc.perform(
                MockMvcRequestBuilders.get(
                    "/profiles/{id}",
                    profiles.add(UUID.randomUUID(), UUID.randomUUID().toString(), "Тимур").id()
                ).header("X-User-Id", UUID.randomUUID().toString())
                    .header("X-User-Role", "ADMIN")
                    .header("X-User-Login", "zoya")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(200)
        );
    }

    @Test
    @DisplayName("пользователь не может получить список всех профилей")
    void dontLetUserListProfiles(@Autowired final MockMvc mvc) throws Exception {
        MatcherAssert.assertThat(
            "пользователь получил список всех профилей",
            mvc.perform(
                MockMvcRequestBuilders.get("/profiles")
                    .header("X-User-Id", UUID.randomUUID().toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "ada")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(403)
        );
    }

    /**
     * Фейки вместо базы и соседних сервисов.
     * @since 1.0
     */
    @TestConfiguration(proxyBeanMethods = false)
    static final class Fakes {

        /**
         * Профили в памяти.
         * @return Профили
         */
        @Bean
        Profiles profiles() {
            return new FakeProfiles();
        }

        /**
         * Характеристики в памяти.
         * @return Характеристики
         */
        @Bean
        Parameters parameters() {
            return new FakeParameters();
        }

        /**
         * Учётки в памяти.
         * @return Учётки
         */
        @Bean
        Accounts accounts() {
            return new FakeAccounts();
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.nimbusds.jose.jwk.RSAKey;
import java.time.Duration;
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
import org.springframework.http.MediaType;
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
    @DisplayName("пользователь не видит сессий другого пользователя")
    void dontLetUserSeeSessionsOfAnother(@Autowired final MockMvc mvc) throws Exception {
        MatcherAssert.assertThat(
            "пользователь увидел чужие сессии",
            mvc.perform(
                MockMvcRequestBuilders.get("/auth/users/{id}/sessions", UUID.randomUUID())
                    .header("X-User-Id", UUID.randomUUID().toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "eve")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("пользователь не может выдать себе роль администратора")
    void dontLetUserGrantHimselfAdmin(
        @Autowired final MockMvc mvc,
        @Autowired final Accounts accounts
    ) throws Exception {
        final UUID id = accounts.add(UUID.randomUUID().toString(), "pwd").id();
        MatcherAssert.assertThat(
            "пользователь смог выдать себе роль",
            mvc.perform(
                MockMvcRequestBuilders.put("/auth/users/{id}/role", id)
                    .header("X-User-Id", id.toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "mallory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"role\":\"ADMIN\"}")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("пользователь может отозвать свои сессии")
    void letsUserRevokeOwnSessions(
        @Autowired final MockMvc mvc,
        @Autowired final Accounts accounts
    ) throws Exception {
        final UUID id = accounts.add(UUID.randomUUID().toString(), "pwd").id();
        MatcherAssert.assertThat(
            "пользователь не смог отозвать свои сессии",
            mvc.perform(
                MockMvcRequestBuilders.post("/auth/users/{id}/revoke", id)
                    .header("X-User-Id", id.toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "bob")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(204)
        );
    }

    @Test
    @DisplayName("аноним не может выйти без аутентификации")
    void dontLetAnonymousLogout(@Autowired final MockMvc mvc) throws Exception {
        MatcherAssert.assertThat(
            "у анонима при выходе не потребовали аутентификации",
            mvc.perform(
                MockMvcRequestBuilders.post("/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refresh_token\":\"x\"}")
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(401)
        );
    }

    @Test
    @DisplayName("аноним может зарегистрироваться")
    void letsAnonymousRegister(@Autowired final MockMvc mvc) throws Exception {
        MatcherAssert.assertThat(
            "аноним не смог зарегистрироваться",
            mvc.perform(
                MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON).content(
                        String.format(
                            "{\"login\":\"%s\",\"password\":\"x\",\"name\":\"Ия\"}",
                            UUID.randomUUID()
                        )
                    )
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(201)
        );
    }

    @Test
    @DisplayName("шлюз без аутентификации узнаёт, активна ли сессия")
    void letsAnonymousAskAboutSession(@Autowired final MockMvc mvc) throws Exception {
        MatcherAssert.assertThat(
            "проверка сессии закрыта для шлюза",
            mvc.perform(MockMvcRequestBuilders.get("/internal/sessions/{id}", UUID.randomUUID()))
                .andReturn().getResponse().getStatus(),
            Matchers.equalTo(200)
        );
    }

    @Test
    @DisplayName("занятый логин отвергается кодом 409")
    void dontRegisterTakenLogin(@Autowired final MockMvc mvc) throws Exception {
        final String body = String.format(
            "{\"login\":\"%s\",\"password\":\"y\",\"name\":\"Ян\"}", UUID.randomUUID()
        );
        mvc.perform(
            MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );
        MatcherAssert.assertThat(
            "занятый логин не дал конфликта",
            mvc.perform(
                MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(409)
        );
    }

    /**
     * Фейки вместо базы, профилей и ключа.
     * @since 1.0
     */
    @TestConfiguration(proxyBeanMethods = false)
    static final class Fakes {

        /**
         * Учётки в памяти.
         * @return Учётки
         */
        @Bean
        Accounts accounts() {
            return new FakeAccounts();
        }

        /**
         * Сессии в памяти.
         * @return Сессии
         */
        @Bean
        Sessions sessions() {
            return new FakeSessions();
        }

        /**
         * Профили в памяти.
         * @return Профили
         */
        @Bean
        Profiles profiles() {
            return new FakeProfiles();
        }

        /**
         * Свежий ключ вместо ключа из окружения.
         * @return Ключ
         * @throws Exception Если ключ не создался
         */
        @Bean
        RSAKey key() throws Exception {
            return new FreshKey().jwk();
        }

        /**
         * Выпуск токенов свежим ключом.
         * @return Выпуск токенов
         * @throws Exception Если ключ не создался
         */
        @Bean
        Token token() throws Exception {
            return new FreshKey().token(Duration.ofMinutes(1));
        }
    }
}

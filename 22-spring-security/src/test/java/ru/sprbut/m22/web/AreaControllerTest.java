/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m22.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.sprbut.m22.security.SecurityConfig;
import ru.sprbut.m22.security.Vault;

/**
 * Слайды 213–218: кто это, что можно и где принимается решение.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebMvcTest(AreaController.class)
@Import({SecurityConfig.class, Vault.class})
@DisplayName("Слайды 213–218: кто это, что можно и где принимается решение")
final class AreaControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("permitAll пропускает в открытую зону без пароля")
    void letsAnyoneIntoPublicArea() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/public/hello"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("без пароля закрытая зона отвечает 401, а не пустым телом")
    void demandsAuthentication() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/me"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("опознанный пользователь попадает в закрытую зону")
    @WithMockUser(username = "anna")
    void letsUserIn() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/me"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("метод узнаёт имя вызвавшего из контекста, а не из аргументов")
    @WithMockUser(username = "anna")
    void namesCurrentUser() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/me"))
            .andExpect(MockMvcResultMatchers.content().string(Matchers.is("anna")));
    }

    @Test
    @DisplayName("нехватка роли — это 403, а не 401: опознан, но не допущен")
    @WithMockUser(username = "anna", roles = "USER")
    void refusesUserWithoutRole() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/admin/report"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("администратора правило на адресе пропускает")
    @WithMockUser(username = "boris", roles = "ADMIN")
    void letsAdminIn() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/admin/report"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("@PreAuthorize закрывает метод, даже когда адрес открыт")
    @WithMockUser(username = "anna", roles = "USER")
    void refusesMethodWithoutRole() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/secret"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("тот же метод с нужной ролью отдаёт содержимое")
    @WithMockUser(username = "boris", roles = "ADMIN")
    void opensMethodWithRole() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/secret"))
            .andExpect(MockMvcResultMatchers.content().string(Matchers.is("код от сейфа")));
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.sprbut.m27.audit.AuditTrail;
import ru.sprbut.m27.extended.ContextMap;
import ru.sprbut.m27.web.IntrospectionController;

/**
 * Защита: решение пустить запрос принимается до контроллера.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebMvcTest(IntrospectionController.class)
@Import(TrackerSecurity.class)
@DisplayName("Защита: решение пустить запрос принимается до контроллера")
final class TrackerSecurityTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    /**
     * Отображение.
     */
    @MockitoBean
    private ContextMap map;

    /**
     * Журнал событий.
     */
    @MockitoBean
    private AuditTrail trail;

    @Test
    @DisplayName("неопознанный запрос к карте контейнера получает 401")
    @WithAnonymousUser
    void demandsAuthentication() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/introspection/beans"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("опознанный пользователь без роли получает 403, а не 401")
    @WithMockUser(username = "anna", roles = "USER")
    void separatesAuthorizationFromAuthentication() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/introspection/beans"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("администратор видит карту контейнера целиком")
    @WithMockUser(username = "boris", roles = "ADMIN")
    void letsAdminSeeTheMap() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/introspection/beans"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

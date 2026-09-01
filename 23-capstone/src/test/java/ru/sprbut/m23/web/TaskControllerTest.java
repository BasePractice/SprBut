/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.web;

import java.time.Instant;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.sprbut.m23.domain.Task;
import ru.sprbut.m23.service.Tasks;

@WebMvcTest(TaskController.class)
/**
 * Срез @WebMvcTest: поднимается только веб-слой.
 * @since 1.0
 */
@DisplayName("Срез @WebMvcTest: поднимается только веб-слой")
final class TaskControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    /**
     * Задачи.
     */
    @MockitoBean
    private Tasks tasks;

    /**
     * Представления.
     */
    @MockitoBean
    private TaskViews views;

    @Test
    @DisplayName("создание задачи отвечает кодом 201")
    void answersCreatedOnNewTask() throws Exception {
        BDDMockito.given(this.tasks.open(ArgumentMatchers.anyString()))
            .willReturn(new Task("проверить срез", Instant.parse("2026-07-30T10:00:00Z")));
        BDDMockito.given(this.views.view(ArgumentMatchers.any()))
            .willReturn(
                TaskView.builder().id(1L).title("проверить срез").status("OPEN").build()
            );
        this.http.perform(
                MockMvcRequestBuilders.post(
                    "/api/tasks"
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"проверить срез\"}")
            )
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("пустое название отбивается проверкой, до сервиса запрос не доходит")
    void dontReachServiceOnInvalidRequest() throws Exception {
        this.http.perform(
                MockMvcRequestBuilders.post(
                    "/api/tasks"
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"  \"}")
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("сообщение о нарушении проверки попадает в тело ответа")
    void explainsValidationFailure() throws Exception {
        this.http.perform(
                MockMvcRequestBuilders.post(
                    "/api/tasks"
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"\"}")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(Matchers.equalTo("title: название задачи обязательно")));
    }

    @Test
    @DisplayName("отсутствующая задача превращается в 404")
    void answersNotFoundOnUnknownTask() throws Exception {
        BDDMockito.willThrow(new IllegalArgumentException("Задачи 7 не существует"))
            .given(this.tasks).start(7L);
        this.http.perform(MockMvcRequestBuilders.post("/api/tasks/7/start"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("исчерпанный лимит превращается в 409")
    void answersConflictOnExhaustedLimit() throws Exception {
        BDDMockito.given(this.tasks.open(ArgumentMatchers.anyString()))
            .willThrow(
                new IllegalStateException("Открытых задач уже 2, лимит исчерпан")
            );
        this.http.perform(
                MockMvcRequestBuilders.post(
                    "/api/tasks"
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"лишняя\"}")
            )
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }
}

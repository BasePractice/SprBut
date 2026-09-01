package ru.sprbut.m23.web;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sprbut.m23.domain.Task;
import ru.sprbut.m23.service.Tasks;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@DisplayName("Срез @WebMvcTest: поднимается только веб-слой")
final class TaskControllerTest {

    @Autowired
    private MockMvc http;

    @MockitoBean
    private Tasks tasks;

    @MockitoBean
    private TaskViews views;

    @Test
    @DisplayName("создание задачи отвечает кодом 201")
    void answersCreatedOnNewTask() throws Exception {
        given(this.tasks.open(anyString()))
            .willReturn(new Task("проверить срез", Instant.parse("2026-07-30T10:00:00Z")));
        given(this.views.view(org.mockito.ArgumentMatchers.any()))
            .willReturn(TaskView.builder().id(1L).title("проверить срез").status("OPEN").build());
        this.http.perform(
                post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"проверить срез\"}")
            )
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("пустое название отбивается проверкой, до сервиса запрос не доходит")
    void dontReachServiceOnInvalidRequest() throws Exception {
        this.http.perform(
                post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"  \"}")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("сообщение о нарушении проверки попадает в тело ответа")
    void explainsValidationFailure() throws Exception {
        this.http.perform(
                post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"\"}")
            )
            .andExpect(jsonPath("$.error").value(equalTo("title: название задачи обязательно")));
    }

    @Test
    @DisplayName("отсутствующая задача превращается в 404")
    void answersNotFoundOnUnknownTask() throws Exception {
        willThrow(new IllegalArgumentException("Задачи 7 не существует"))
            .given(this.tasks).start(7L);
        this.http.perform(post("/api/tasks/7/start"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("исчерпанный лимит превращается в 409")
    void answersConflictOnExhaustedLimit() throws Exception {
        given(this.tasks.open(anyString()))
            .willThrow(new IllegalStateException("Открытых задач уже 2, лимит исчерпан"));
        this.http.perform(
                post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"лишняя\"}")
            )
            .andExpect(status().isConflict());
    }
}

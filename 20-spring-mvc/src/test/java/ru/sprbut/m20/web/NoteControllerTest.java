/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m20.web;

import java.util.List;
import java.util.Optional;
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
import ru.sprbut.m20.domain.Note;
import ru.sprbut.m20.domain.Notes;

/**
 * Слайды 194–199: аннотации, аргументы метода, ResponseEntity и советы.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebMvcTest(NoteController.class)
@DisplayName("Слайды 194–199: аннотации, аргументы метода, ResponseEntity и советы")
final class NoteControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    /**
     * Заметки.
     */
    @MockitoBean
    private Notes notes;

    @Test
    @DisplayName("возвращённый объект уходит в тело ответа, а не в поиск представления")
    void writesObjectToBody() throws Exception {
        BDDMockito.given(this.notes.all()).willReturn(List.of(new Note(1L, "первая")));
        this.http.perform(MockMvcRequestBuilders.get("/api/notes"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].text", Matchers.is("первая")));
    }

    @Test
    @DisplayName("@PathVariable достаёт номер из пути")
    void readsPathVariable() throws Exception {
        BDDMockito.given(this.notes.find(7L))
            .willReturn(Optional.of(new Note(7L, "седьмая")));
        this.http.perform(MockMvcRequestBuilders.get("/api/notes/7"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.text", Matchers.is("седьмая")));
    }

    @Test
    @DisplayName("несуществующая заметка отвечает кодом 404, а не пустым телом")
    void answersNotFoundOnMissingNote() throws Exception {
        BDDMockito.given(this.notes.find(ArgumentMatchers.anyLong()))
            .willReturn(Optional.empty());
        this.http.perform(MockMvcRequestBuilders.get("/api/notes/404"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("@RequestParam достаёт параметр строки запроса")
    void readsRequestParam() throws Exception {
        BDDMockito.given(this.notes.search("сад"))
            .willReturn(List.of(new Note(3L, "полить сад")));
        this.http.perform(MockMvcRequestBuilders.get("/api/notes/search").param("text", "сад"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(3)));
    }

    @Test
    @DisplayName("создание заметки отвечает кодом 201")
    void answersCreatedOnNewNote() throws Exception {
        BDDMockito.given(this.notes.add(ArgumentMatchers.anyString()))
            .willReturn(new Note(1L, "купить хлеб"));
        this.http.perform(
            MockMvcRequestBuilders.post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"купить хлеб\"}")
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("ответ 201 несёт заголовок Location с адресом созданной заметки")
    void answersWithLocationHeader() throws Exception {
        BDDMockito.given(this.notes.add(ArgumentMatchers.anyString()))
            .willReturn(new Note(5L, "купить хлеб"));
        this.http.perform(
            MockMvcRequestBuilders.post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"купить хлеб\"}")
        ).andExpect(MockMvcResultMatchers.header().string("Location", "/api/notes/5"));
    }

    @Test
    @DisplayName("пустой текст не проходит проверку и отвечает кодом 400")
    void rejectsBlankText() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"  \"}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("совет собирает нарушенные правила в тело ответа")
    void explainsWhatWentWrong() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.containsString("обязателен"))
        );
    }

    @Test
    @DisplayName("обработчик работает в потоке контейнера, а не в потоке вызывающего")
    void runsInContainerThread() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/notes/thread")
        ).andExpect(
            MockMvcResultMatchers.content().string(Matchers.not(Matchers.emptyString()))
        );
    }
}

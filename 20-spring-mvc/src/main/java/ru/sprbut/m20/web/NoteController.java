/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m20.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m20.domain.Note;
import ru.sprbut.m20.domain.Notes;

/**
 * Слайды 194–198: аннотации, аргументы метода и {@code ResponseEntity}.
 *
 * <p>{@code @RestController} — это {@code @Controller} плюс
 * {@code @ResponseBody} на каждом методе: возвращённый объект уходит
 * в тело ответа, а не ищется как имя представления. Разницу видно рядом,
 * в {@link PageController}.</p>
 *
 * <p>Каждый метод здесь показывает свой способ получить данные запроса:
 * путь ({@code @PathVariable}), строка запроса ({@code @RequestParam}),
 * тело ({@code @RequestBody}). Ни один из них не читает
 * {@code HttpServletRequest} — этим занят {@code DispatcherServlet},
 * а контроллер получает уже разобранные значения.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/notes")
public final class NoteController {

    /**
     * Заметки.
     */
    private final Notes notes;

    /**
     * Основной конструктор.
     * @param notes Заметки
     */
    public NoteController(final Notes notes) {
        this.notes = notes;
    }

    /**
     * Все заметки: метод возвращает объект, а не имя представления.
     * @return Все заметки
     */
    @GetMapping
    public List<Note> all() {
        return this.notes.all();
    }

    /**
     * Слайд 197: {@code @PathVariable} достаёт часть пути.
     * @param id Номер заметки
     * @return Заметка или ответ 404 без тела
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> one(@PathVariable final long id) {
        return this.notes.find(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Слайд 197: {@code @RequestParam} достаёт параметр строки запроса.
     * @param text Искомая подстрока
     * @return Заметки, содержащие подстроку
     */
    @GetMapping("/search")
    public List<Note> search(@RequestParam final String text) {
        return this.notes.search(text);
    }

    /**
     * Слайды 197–198: тело запроса, проверка и полный ответ.
     *
     * <p>{@code ResponseEntity} собирает три вещи разом: код состояния,
     * заголовки и тело. Заголовок {@code Location} с адресом созданного
     * ресурса — то, чем ответ 201 отличается от 200.</p>
     *
     * @param request Тело запроса
     * @return Созданная заметка с кодом 201 и заголовком Location
     */
    @PostMapping
    public ResponseEntity<Note> add(@Valid @RequestBody final NewNoteRequest request) {
        final Note note = this.notes.add(request.text());
        return ResponseEntity.created(URI.create(String.format("/api/notes/%d", note.id())))
            .body(note);
    }

    /**
     * Слайд 200: имя потока, в котором работает обработчик.
     *
     * <p>Ответ на этот запрос — не число и не строка данных, а факт: запрос
     * обрабатывается потоком из пула сервлет-контейнера, и поток занят до
     * конца обработки. Тот же метод в модуле 21 отвечает именем потока
     * событийного цикла — сравнение и есть содержание темы.</p>
     *
     * @return Имя потока, обрабатывающего запрос
     */
    @GetMapping("/thread")
    public String thread() {
        return Thread.currentThread().getName();
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.sprbut.m27.domain.TaskStatus;
import ru.sprbut.m27.service.Tasks;

/**
 * HTTP-вход в трекер.
 *
 * <p>{@code @RestController} — это {@code @Controller} плюс {@code @ResponseBody},
 * ровно как сказано на слайде про аннотации Spring. Первая делает класс бином
 * и обработчиком запросов, вторая избавляет от {@code ResponseEntity} вокруг
 * каждого возвращаемого объекта.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/tasks")
public final class TaskController {

    /**
     * Задачи.
     */
    private final Tasks tasks;

    /**
     * Представления.
     */
    private final TaskViews views;

    /**
     * Поток задач.
     */
    private final TaskFeed feed;

    /**
     * Основной конструктор.
     * @param tasks Задачи
     * @param views Представления
     * @param feed Поток задач
     */
    public TaskController(final Tasks tasks, final TaskViews views, final TaskFeed feed) {
        this.tasks = tasks;
        this.views = views;
        this.feed = feed;
    }

    /**
     * Статус.
     * @param status Статус
     * @return Статус
     */
    @GetMapping
    public List<TaskView> byStatus(final @RequestParam(defaultValue = "OPEN") TaskStatus status) {
        return this.views.views(this.tasks.byStatus(status));
    }

    /**
     * Те же задачи, отданные потоком событий.
     * @param status Статус
     * @return Те же задачи, отданные потоком событий
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TaskView> stream(final @RequestParam(defaultValue = "OPEN") TaskStatus status) {
        return this.feed.stream(status);
    }

    /**
     * Открытие.
     * @param request Запрос
     * @return Открытие
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskView open(final @Valid @RequestBody NewTaskRequest request) {
        return this.views.view(this.tasks.open(request.title()));
    }

    /**
     * Старт.
     * @param id Идентификатор
     * @return Старт
     */
    @PostMapping("/{id}/start")
    public TaskView start(final @PathVariable long id) {
        return this.views.view(this.tasks.start(id));
    }

    /**
     * Значение {@code finish}.
     * @param id Идентификатор
     * @return Значение {@code finish}
     */
    @PostMapping("/{id}/finish")
    public TaskView finish(final @PathVariable long id) {
        return this.views.view(this.tasks.finish(id));
    }
}

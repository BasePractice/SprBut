package ru.sprbut.m23.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m23.domain.TaskStatus;
import ru.sprbut.m23.service.Tasks;

/**
 * HTTP-вход в трекер.
 * <p>
 * {@code @RestController} — это {@code @Controller} плюс {@code @ResponseBody},
 * ровно как сказано на слайде про аннотации Spring. Первая делает класс бином
 * и обработчиком запросов, вторая избавляет от {@code ResponseEntity} вокруг
 * каждого возвращаемого объекта.
 */
@RestController
@RequestMapping("/api/tasks")
public final class TaskController {

    private final Tasks tasks;

    private final TaskViews views;

    public TaskController(Tasks tasks, TaskViews views) {
        this.tasks = tasks;
        this.views = views;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskView open(@Valid @RequestBody NewTaskRequest request) {
        return this.views.view(this.tasks.open(request.title()));
    }

    @PostMapping("/{id}/start")
    public TaskView start(@PathVariable long id) {
        return this.views.view(this.tasks.start(id));
    }

    @PostMapping("/{id}/finish")
    public TaskView finish(@PathVariable long id) {
        return this.views.view(this.tasks.finish(id));
    }

    @GetMapping
    public List<TaskView> byStatus(@RequestParam(defaultValue = "OPEN") TaskStatus status) {
        return this.views.views(this.tasks.byStatus(status));
    }
}

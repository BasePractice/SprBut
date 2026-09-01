package ru.sprbut.m23.domain;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.ApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@DisplayName("Срез @DataJpaTest: только JPA, без веба и аспектов")
final class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("сущность получает идентификатор от базы, а не от кода")
    void assignsIdentityOnSave() {
        assertThat(
            "database cannot assign the identity on save",
            this.repository.save(
                new Task("проверить срез", Instant.parse("2026-07-30T10:00:00Z"))
            ).id(),
            notNullValue()
        );
    }

    @Test
    @DisplayName("запрос выводится из имени метода, реализации в исходниках нет")
    void derivesQueryFromMethodName() {
        this.repository.save(new Task("открытая", Instant.parse("2026-07-30T10:00:00Z")));
        assertThat(
            "query derived from the method name cannot find the open task",
            this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN),
            hasSize(1)
        );
    }

    @Test
    @DisplayName("сортировка из имени метода отдаёт новые задачи первыми")
    void sortsNewestFirst() {
        this.repository.save(new Task("старая", Instant.parse("2026-07-29T10:00:00Z")));
        this.repository.save(new Task("новая", Instant.parse("2026-07-30T10:00:00Z")));
        assertThat(
            "derived ordering cannot put the newest task first",
            this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN)
                .stream().map(Task::title).toList(),
            contains("новая", "старая")
        );
    }

    @Test
    @DisplayName("срез не поднимает веб-слой — контроллера в контексте нет")
    void dontLoadWebLayer() {
        assertThat(
            "JPA slice cannot leave the controllers out of the context",
            this.context.getBeanNamesForType(ru.sprbut.m23.web.TaskController.class).length,
            equalTo(0)
        );
    }
}

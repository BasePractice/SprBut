package ru.sprbut.m23.web;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * Представление задачи для API.
 * <p>
 * Здесь курс замыкается сам на себя: {@code @Value} и {@code @Builder} —
 * это Lombok, то есть annotation processor, который дописывает класс
 * во время компиляции. Поля становятся {@code private final}, класс —
 * {@code final}, конструктор и билдер генерируются.
 * <p>
 * Соседний {@link NewTaskRequest} делает почти то же самое штатным
 * {@code record}: наглядное сравнение генерации кода и языковой конструкции.
 */
@Value
@Builder
public class TaskView {

    Long id;

    String title;

    String status;

    Instant created;
}

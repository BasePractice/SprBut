package ru.sprbut.m23.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Тело запроса на создание задачи.
 * <p>
 * {@code record} закрывает ровно ту потребность, ради которой в JavaBeans
 * заводили геттеры и сеттеры: неизменяемые данные с готовыми
 * {@code equals}, {@code hashCode} и {@code toString}.
 * <p>
 * Аннотации проверки — снова метаданные: сами по себе они ничего не запрещают,
 * работать их заставляет {@code @Valid} в контроллере.
 */
public record NewTaskRequest(

    @NotBlank(message = "название задачи обязательно")
    @Size(max = 200, message = "название длиннее 200 символов")
    String title
) {
}

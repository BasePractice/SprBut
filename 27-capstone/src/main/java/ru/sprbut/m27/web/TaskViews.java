/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.web;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sprbut.m27.domain.Task;

/**
 * Преобразование сущности в представление.
 *
 * <p>Реализации в исходниках нет — её пишет MapStruct во время компиляции,
 * а {@code componentModel = "spring"} помечает сгенерированный класс
 * как {@code @Component}, превращая его в обычный бин.</p>
 *
 * <p>Разница с рефлексией принципиальна: сопоставление полей вычисляется один раз
 * компилятором, а в рантайме остаются прямые вызовы методов. Ошибка в имени поля
 * ломает сборку, а не прод.</p>
 *
 * <p>Источники указаны явными выражениями, и это не прихоть: {@link Task} назван
 * по правилам Elegant Objects — {@code title()}, а не {@code getTitle()}.
 * MapStruct же, как и вся экосистема вокруг JavaBeans, ищет приставку {@code get}.
 * Здесь ровно та цена, о которой говорит слайд про JavaBeans: соглашение
 * тридцатилетней давности всё ещё определяет, что инструменты считают «свойством».</p>
 *
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface TaskViews {

    /**
     * Переводит задачу в её представление для API.
     * @param task Задача
     * @return Переводит задачу в её представление для API
     */
    @Mapping(target = "id", expression = "java(task.id())")
    @Mapping(target = "title", expression = "java(task.title())")
    @Mapping(target = "status", expression = "java(task.status().name())")
    @Mapping(target = "created", expression = "java(task.created())")
    TaskView view(Task task);

    /**
     * Переводит список задач.
     * @param tasks Задачи
     * @return Переводит список задач
     */
    List<TaskView> views(List<Task> tasks);
}

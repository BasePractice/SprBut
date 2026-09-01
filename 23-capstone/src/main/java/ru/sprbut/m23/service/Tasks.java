/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.service;

import java.util.List;
import ru.sprbut.m23.domain.Task;
import ru.sprbut.m23.domain.TaskStatus;

/**
 * Прикладной сценарий работы с задачами.
 *
 * <p>Интерфейс нужен не «для абстракции», а по делу: у бина с интерфейсом Spring
 * строит JDK dynamic proxy, и реализация может оставаться {@code final}.
 * Без интерфейса в дело пошёл бы CGLIB, которому нужен наследуемый класс.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Tasks {

    /**
     * Заводит новую задачу.
     * @param title Название
     * @return Заводит новую задачу
     */
    Task open(String title);

    /**
     * Переводит задачу в работу.
     * @param id Идентификатор
     * @return Переводит задачу в работу
     */
    Task start(long id);

    /**
     * Закрывает задачу.
     * @param id Идентификатор
     * @return Закрывает задачу
     */
    Task finish(long id);

    /**
     * Задачи в указанном состоянии.
     * @param status Статус
     * @return Задачи в указанном состоянии
     */
    List<Task> byStatus(TaskStatus status);
}

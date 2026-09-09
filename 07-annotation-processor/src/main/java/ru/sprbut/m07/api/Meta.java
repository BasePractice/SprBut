/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m07.api;

/**
 * Паспорт класса: то, что компилятор знал о нём в момент сборки.
 *
 * <p>Интерфейс объявлен здесь, а не рядом с процессором, по той же причине,
 * по которой генератор и его результат вообще разделены: сгенерированный
 * класс нужен в runtime, а процессор — только компилятору.</p>
 *
 * @since 1.0
 */
public interface Meta {

    /**
     * Полное имя класса, для которого собран паспорт.
     * @return Полное имя класса
     */
    String origin();

    /**
     * Сколько методов было в классе на момент компиляции.
     * @return Число методов
     */
    int methods();
}

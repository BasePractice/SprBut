/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sprbut.m20.domain.Notes;

/**
 * Слайд 195: {@code @Controller} против {@code @RestController}.
 *
 * <p>Возвращённая строка здесь — не тело ответа, а имя представления:
 * его будет искать {@code ViewResolver}. Данные для представления кладутся
 * в {@code Model}. Разница с {@link NoteController} ровно в одной
 * аннотации, и она меняет смысл возвращаемого значения.</p>
 *
 * @since 1.0
 */
@Controller
public final class PageController {

    /**
     * Заметки.
     */
    private final Notes notes;

    /**
     * Основной конструктор.
     * @param notes Заметки
     */
    public PageController(final Notes notes) {
        this.notes = notes;
    }

    /**
     * Страница со списком заметок.
     * @param model Модель представления
     * @return Имя представления, а не содержимое ответа
     */
    @GetMapping("/notes")
    public String page(final Model model) {
        model.addAttribute("notes", this.notes.all());
        return "notes";
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m28.aspects.Trimmed;

/**
 * Контроллер под аспектом.
 *
 * <p>Единственный не-{@code final} класс модуля, и это условие примера.
 * {@code TrimmingAspect} работает через прокси, а прокси для класса без
 * интерфейсов строится наследованием. С {@code final} контекст упал бы
 * на старте, не создав прокси.</p>
 *
 * <p>Сам метод ничего не обрезает: пробелы по краям обоих параметров
 * срезаны аспектом до вызова.</p>
 *
 * @since 1.0
 */
@Trimmed
@RestController
@RequestMapping("/api/search")
public class SearchController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SearchController() {
        // нечего инициализировать
    }

    /**
     * Поиск: параметры приходят уже обрезанными.
     * @param query Что искать
     * @param city Где искать
     * @return Условия поиска в скобках
     */
    @GetMapping
    public String search(@RequestParam final String query, @RequestParam final String city) {
        return String.format("[%s] в [%s]", query, city);
    }
}

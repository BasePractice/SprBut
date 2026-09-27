/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sprbut.m28.returns.Lines;

/**
 * Обычный {@code @Controller}: в нём работает свой обработчик результата.
 *
 * <p>Два метода возвращают один и тот же {@link Lines}, а ответы у них
 * разные. Первый отдаёт {@code LinesReturnValueHandler} построчным
 * текстом. Второй помечен {@code @ResponseBody}, и за его результат
 * берётся штатный обработчик, стоящий раньше в очереди: {@code Lines}
 * уходит в Jackson. Ровно так же вёл бы себя любой метод
 * {@code @RestController}.</p>
 *
 * @since 1.0
 */
@Controller
@RequestMapping("/plain/users")
public final class PlainController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public PlainController() {
        // нечего инициализировать
    }

    /**
     * Пользователи построчно.
     * @return Строки ответа
     */
    @GetMapping
    public Lines lines() {
        return new Lines(List.of("ivan", "petr"));
    }

    /**
     * Те же пользователи, но через {@code @ResponseBody}.
     * @return Строки ответа
     */
    @ResponseBody
    @GetMapping("/json")
    public Lines json() {
        return new Lines(List.of("ivan", "petr"));
    }
}

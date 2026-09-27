/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import java.util.Objects;
import java.util.concurrent.Callable;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m28.context.Correlation;

/**
 * Три способа получить то, что положил в запрос фильтр.
 *
 * <p>{@code @RequestAttribute} берёт атрибут прямо из запроса — это
 * самый короткий путь, но доступен он только методу контроллера.
 * Бин области запроса {@link Correlation} внедрён в одиночку-контроллер
 * через прокси и отвечает значением текущего запроса. MDC журнала
 * читается откуда угодно в том же потоке.</p>
 *
 * <p>Асинхронные методы делают то же из рабочего потока. Бин области
 * запроса там находится сам: запрос в рабочий поток переносит Spring.
 * MDC Spring не переносит, и без {@code MdcPropagation} второй
 * асинхронный метод вернул бы пустоту.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/trace")
public final class TraceController {

    /**
     * Идентификатор текущего запроса.
     */
    private final Correlation correlation;

    /**
     * Основной конструктор.
     * @param correlation Идентификатор текущего запроса
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public TraceController(final @NonNull Correlation correlation) {
        this.correlation = Objects.requireNonNull(
            correlation, "идентификатор запроса не внедрён"
        );
    }

    /**
     * Идентификатор из атрибута запроса.
     * @param id Идентификатор
     * @return Идентификатор
     */
    @GetMapping("/attribute")
    public String attribute(@RequestAttribute("sprbut.correlation") final String id) {
        return id;
    }

    /**
     * Идентификатор из бина области запроса.
     * @return Идентификатор
     */
    @GetMapping("/bean")
    public String bean() {
        return this.correlation.id();
    }

    /**
     * Идентификатор из бина области запроса, прочитанный в другом потоке.
     * @return Задача, которая вернёт идентификатор
     */
    @GetMapping("/async/bean")
    public Callable<String> asyncBean() {
        return this.correlation::id;
    }

    /**
     * Идентификатор из MDC журнала, прочитанный в другом потоке.
     * @return Задача, которая вернёт идентификатор
     */
    @GetMapping("/async/log")
    public Callable<String> asyncLog() {
        return () -> MDC.get("correlation");
    }
}

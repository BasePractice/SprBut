/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m27.audit.AuditTrail;
import ru.sprbut.m27.extended.BeanCard;
import ru.sprbut.m27.extended.ContextMap;

/**
 * Приложение, объясняющее само себя, по HTTP.
 *
 * <p>{@code /api/introspection/beans} показывает, чем бины стали внутри контейнера,
 * а {@code /api/introspection/audit} — что успел записать аспект.
 * Вместе они отвечают на вопрос, ради которого затевался весь курс:
 * почему код в исходниках и поведение в рантайме — это не одно и то же.</p>
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/introspection")
public final class IntrospectionController {

    /**
     * Отображение.
     */
    private final ContextMap map;

    /**
     * Журнал событий.
     */
    private final AuditTrail trail;

    /**
     * Основной конструктор.
     * @param map Отображение
     * @param trail Журнал событий
     */
    public IntrospectionController(final ContextMap map, final AuditTrail trail) {
        this.map = map;
        this.trail = trail;
    }

    /**
     * Объекты.
     * @return Объекты
     */
    @GetMapping("/beans")
    public List<BeanCard> beans() {
        return this.map.cards();
    }

    /**
     * Аудит.
     * @return Аудит
     */
    @GetMapping("/audit")
    public List<String> audit() {
        return this.trail.records();
    }
}

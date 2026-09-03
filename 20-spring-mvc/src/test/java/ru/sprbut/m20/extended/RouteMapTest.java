/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m20.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sprbut.m20.web.NoteController;

/**
 * Расширенный пример: приложение показывает собственную таблицу маршрутов.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Расширенный пример: приложение показывает собственную таблицу маршрутов")
final class RouteMapTest {

    /**
     * Карта маршрутов.
     */
    @Autowired
    private RouteMap map;

    @Test
    @DisplayName("маршруты контроллера видны в таблице, собранной контейнером")
    void findsControllerRoutes() {
        MatcherAssert.assertThat(
            "маршруты контроллера не попали в таблицу",
            this.map.of(NoteController.class),
            Matchers.not(Matchers.empty())
        );
    }

    @Test
    @DisplayName("шаблон пути в таблице совпадает с тем, что написано в аннотации")
    void keepsPathPattern() {
        MatcherAssert.assertThat(
            "шаблон пути потерялся по дороге от аннотации до реестра",
            this.map.cards().stream().flatMap(card -> card.patterns().stream()).toList(),
            Matchers.hasItem("/api/notes/{id}")
        );
    }

    @Test
    @DisplayName("HTTP-метод маршрута берётся из сокращённой аннотации")
    void keepsHttpMethod() {
        MatcherAssert.assertThat(
            "метод POST не доехал от @PostMapping до реестра маршрутов",
            this.map.of(NoteController.class).stream()
                .flatMap(card -> card.methods().stream())
                .toList(),
            Matchers.hasItem("POST")
        );
    }

    @Test
    @DisplayName("карточка маршрута называет метод, который будет вызван")
    void namesHandlerMethod() {
        MatcherAssert.assertThat(
            "карточка не говорит, какой метод обработает запрос",
            this.map.of(NoteController.class).stream().map(RouteCard::handler).toList(),
            Matchers.hasItem("NoteController.add")
        );
    }
}

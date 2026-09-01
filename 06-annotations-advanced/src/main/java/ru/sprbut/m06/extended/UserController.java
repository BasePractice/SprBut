/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.extended;

/**
 * Подопытный контроллер: маршруты, объявленные через композиции разной глубины.
 *
 * <p>Пять методов покрывают все случаи слияния: прямая аннотация, композиция
 * первого уровня, переопределение одноимённого элемента, композиция второго
 * уровня и сломанный алиас.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public class UserController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public UserController() {
        // нечего инициализировать
    }

    /**
     * Прямая аннотация без всякой композиции — контрольный случай.
     */
    @RequestMapping(path = "/raw", method = HttpMethod.POST, produces = "text/plain")
    public void raw() {
    }

    /**
     * Композиция первого уровня: путь приходит через {@code @AliasFor}.
     */
    @GetMapping("/users")
    public void list() {
    }

    /**
     * Одноимённый элемент переопределяет мета-аннотацию без всякого алиаса.
     */
    @GetMapping(value = "/users/active", produces = {"application/json", "application/xml"})
    public void listActive() {
    }

    /**
     * Композиция второго уровня: до {@code @RequestMapping} два шага.
     */
    @GetJson("/users/json")
    public void json() {
    }

    /**
     * Алиас на несуществующий элемент — ошибка, которую видно только при чтении.
     */
    @BrokenMapping("/broken")
    public void broken() {
    }

    /**
     * Метод без аннотаций вовсе.
     */
    public void plain() {
    }
}

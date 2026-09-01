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
     * Метод без аннотаций вовсе.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public void plain() {
        // тело намеренно пустое
    }

    /**
     * Прямая аннотация без всякой композиции — контрольный случай.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @RequestMapping(path = "/raw", method = HttpMethod.POST, produces = "text/plain")
    public void raw() {
        // тело намеренно пустое
    }

    /**
     * Композиция первого уровня: путь приходит через {@code @AliasFor}.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @GetMapping("/users")
    public void list() {
        // тело намеренно пустое
    }

    /**
     * Одноимённый элемент переопределяет мета-аннотацию без всякого алиаса.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @GetMapping(value = "/users/active", produces = {"application/json", "application/xml"})
    public void listActive() {
        // тело намеренно пустое
    }

    /**
     * Композиция второго уровня: до {@code @RequestMapping} два шага.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @GetJson("/users/json")
    public void json() {
        // тело намеренно пустое
    }

    /**
     * Алиас на несуществующий элемент — ошибка, которую видно только при чтении.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @BrokenMapping("/broken")
    public void broken() {
        // тело намеренно пустое
    }
}

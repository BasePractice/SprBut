/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.scopes;

/**
 * Prototype-бин: контейнер должен отдавать новый экземпляр на каждый запрос.
 *
 * <p>Класс намеренно не {@code final}. С {@code proxyMode = TARGET_CLASS} Spring
 * строит CGLIB-подкласс, а унаследоваться от финального класса нельзя —
 * это тот случай, когда требование фреймворка перевешивает привычку
 * закрывать классы от наследования.</p>
 *
 * @since 1.0
 */
public class Ticket {

    /**
     * Номер, присвоенный при создании экземпляра.
     */
    private final int number;

    /**
     * Основной конструктор.
     * Обращение к счётчику здесь и есть предмет разговора: каждый новый
     * экземпляр прототипа должен получить свой номер.
     * @param serial Счётчик выданных номеров
     * @checkstyle ConstructorsCodeFreeCheck (5 lines)
     */
    public Ticket(final Serial serial) {
        this.number = serial.next();
    }

    /**
     * Номер, присвоенный при создании экземпляра.
     * @return Номер талона
     */
    public int number() {
        return this.number;
    }
}

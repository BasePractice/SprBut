/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// класс намеренно не final: ByteBuddy строит его подкласс —
// это и есть третий механизм, с которым сравнивают рефлексию и APT
// @checkstyle NonStaticMethodCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

/**
 * Сервис <b>без интерфейса</b> — JDK-прокси такой класс проксировать не умеет.
 *
 * <p>Именно этот случай заставляет Spring переключаться на CGLIB: обёртка строится
 * подклассом, а не реализацией контракта. Отсюда же и требование, чтобы класс
 * не был {@code final}.</p>
 *
 * @since 1.0
 */
public class AuditService {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AuditService() {
        // нечего инициализировать
    }

    /**
     * Записывает событие.
     * @param event Событие
     * @return Записывает событие
     */
    public String record(final String event) {
        return String.format("записано: %s", event);
    }

    /**
     * Число записей.
     * @return Число записей
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public int size() {
        return 0;
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Слайд 31: что модульная система разрешает делать с чужим классом.
 *
 * <p>JPMS различает два уровня, и путать их дорого:
 * <ul>
 * <li><b>exports</b> — типы пакета видны компилятору и обычному коду;</li>
 * <li><b>opens</b> — пакет дополнительно открыт для <i>глубокой рефлексии</i>,
 * то есть {@code setAccessible(true)} на его закрытых членах разрешён.</li>
 * </ul>
 * Модуль {@code java.base} экспортирует почти всё, а открывает почти ничего.
 * Отсюда и требование Spring, Hibernate и Jackson к флагам вида
 * {@code --add-opens java.base/java.lang=ALL-UNNAMED}.</p>
 *
 * @since 1.0
 */
public final class ModuleAccess {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public ModuleAccess(final Class<?> type) {
        this.type = type;
    }

    /**
     * Открыт ли пакет для глубокой рефлексии из нашего модуля.
     * Штатный способ проверить доступ, не ловя исключение.
     * @return Открыт ли пакет для глубокой рефлексии из нашего модуля
     */
    public boolean open() {
        return this.type.getModule()
            .isOpen(this.type.getPackageName(), ModuleAccess.class.getModule());
    }

    /**
     * Экспортирован ли пакет — то есть виден ли он обычному коду.
     * @return Экспортирован ли пакет — то есть виден ли он обычному коду
     */
    public boolean exported() {
        return this.type.getModule()
            .isExported(this.type.getPackageName(), ModuleAccess.class.getModule());
    }

    /**
     * Имя модуля-владельца. Код, запущенный с classpath, попадает
     * в безымянный модуль, и здесь будет {@code null}.
     * @return Имя модуля-владельца или {@code null} для безымянного модуля
     */
    public String moduleName() {
        return this.type.getModule().getName();
    }
}

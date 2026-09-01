/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.extended;

import java.util.List;

/**
 * Карточка одного бина: что это, как долго живёт и во что обёрнуто.
 *
 * <p>Четыре факта, которых достаточно, чтобы объяснить почти любое загадочное
 * поведение приложения: подменённая область видимости, потерянный
 * {@code @Transactional}, неперехваченный self-invocation.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class BeanCard {

    /**
     * Имя.
     */
    private final String name;

    /**
     * Тип.
     */
    private final String type;

    /**
     * Область видимости.
     */
    private final String scope;

    /**
     * Значение {@code audited}.
     */
    private final List<String> audited;

    /**
     * Основной конструктор.
     *
     * <p>Копия списка операций снимается здесь: карточка обязана быть неизменяемой.</p>
     *
     * @param name Имя
     * @param type Тип
     * @param scope Область видимости
     * @param audited Имена аудируемых операций
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public BeanCard(final String name, final String type, final String scope,
        final List<String> audited) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.audited = List.copyOf(audited);
    }

    /**
     * Имя бина в контейнере.
     * @return Имя бина в контейнере
     */
    public String name() {
        return this.name;
    }

    /**
     * Настоящий класс за прокси, если прокси есть.
     * @return Настоящий класс за прокси, если прокси есть
     */
    public String type() {
        return this.type;
    }

    /**
     * Область видимости: singleton, prototype или иная.
     * @return Область видимости: singleton, prototype или иная
     */
    public String scope() {
        return this.scope;
    }

    /**
     * Методы, помеченные аннотацией аудита.
     * @return Методы, помеченные аннотацией аудита
     */
    public List<String> audited() {
        return this.audited;
    }
}

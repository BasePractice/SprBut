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
 * <p>{@code record} здесь не только короче обычного класса: Jackson знает
 * про компоненты записи и сериализует их без геттеров в стиле JavaBeans,
 * тогда как у финального класса с методами {@code name()} и {@code type()}
 * он не нашёл бы ни одного свойства.</p>
 *
 * @param name Имя бина в контейнере
 * @param type Настоящий класс за прокси, если прокси есть
 * @param scope Область видимости: singleton, prototype или иная
 * @param audited Методы, помеченные аннотацией аудита
 * @since 1.0
 */
public record BeanCard(String name, String type, String scope, List<String> audited) {

    /**
     * Компактный конструктор.
     *
     * <p>Копия списка операций снимается здесь: карточка обязана быть неизменяемой.</p>
     */
    public BeanCard {
        audited = List.copyOf(audited);
    }
}

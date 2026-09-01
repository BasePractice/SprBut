/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.extended;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.sprbut.m09.ReflectiveMapper;
import ru.sprbut.m09.model.UserDto;
import ru.sprbut.m09.model.UserEntity;

/**
 * Что пришлось бы объявить в {@code RuntimeHints}, чтобы каждая реализация
 * заработала в native image.
 *
 * <p>Здесь цена рефлексии становится наглядной: список для неё не пуст и растёт
 * с каждым новым свойством, причём вычислен он тоже рефлексией. Для генерации
 * кода список пуст — обращений к метаданным просто нет. А для байткода вопрос
 * не имеет смысла: класса ещё не существует в момент сборки образа.</p>
 *
 * @since 1.0
 */
public final class RequiredHints {

    /**
     * Рефлективный вариант.
     */
    private final ReflectiveMapper reflective;

    /**
     * Основной конструктор.
     */
    public RequiredHints() {
        this(new ReflectiveMapper());
    }

    /**
     * Основной конструктор.
     * @param reflective Рефлективный вариант
     */
    public RequiredHints(final ReflectiveMapper reflective) {
        this.reflective = reflective;
    }

    /**
     * Подсказки по каждой реализации.
     * @return Подсказки по каждой реализации
     */
    public Map<String, List<String>> byMapper() {
        final Map<String, List<String>> hints = new LinkedHashMap<>();
        hints.put("ReflectiveMapper", this.accessors());
        hints.put("GeneratedStyleMapper", List.of());
        hints.put(
            "BytecodeMapper",
            List.of("класс генерируется в runtime — native image неприменим")
        );
        return Map.copyOf(hints);
    }

    /**
     * Методы, к которым рефлексивный маппер обращается по именам.
     * @return Методы, к которым рефлексивный маппер обращается по именам
     */
    public List<String> accessors() {
        final List<String> needed = new ArrayList<>(0);
        for (final String property : this.reflective.propertyNames()) {
            final String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            needed.add(UserEntity.class.getSimpleName() + "#get" + suffix);
            needed.add(UserDto.class.getSimpleName() + "#set" + suffix);
        }
        needed.sort(String::compareTo);
        return List.copyOf(needed);
    }
}

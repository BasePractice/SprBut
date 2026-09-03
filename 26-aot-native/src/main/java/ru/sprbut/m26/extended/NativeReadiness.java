/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m26.extended;

import java.util.Collection;
import java.util.List;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

/**
 * <b>Расширенный пример модуля 26.</b>
 *
 * <p>Готовность приложения к native image: сверяет классы, которые создаются
 * рефлексией, с тем, что реально попало в {@code RuntimeHints}.</p>
 *
 * <p>Смысл упражнения в том, что обычные тесты такую дыру не видят. На JVM
 * незарегистрированный класс работает как ни в чём не бывало; сборка native
 * тоже проходит успешно — падает уже готовый образ, в рантайме, на клиенте.
 * Единственный способ поймать это заранее — проверять подсказки, а не поведение.</p>
 *
 * <p>Тот же приём использует {@code RuntimeHintsPredicates} в тестах самого Spring.</p>
 *
 * @since 1.0
 */
public final class NativeReadiness {

    /**
     * Подсказки, собранные приложением для сборщика образа.
     */
    private final RuntimeHints hints;

    /**
     * Основной конструктор.
     * @param hints Подсказки, собранные приложением
     */
    public NativeReadiness(final RuntimeHints hints) {
        this.hints = hints;
    }

    /**
     * Переживёт ли создание этого класса рефлексией сборку в native image.
     * @param type Класс, который создаётся рефлексией
     * @return Признак того, что класс объявлен в подсказках
     */
    public boolean covers(final Class<?> type) {
        return RuntimeHintsPredicates.reflection()
            .onType(type)
            .test(this.hints);
    }

    /**
     * Классы, которые рефлексия использует, а подсказки не упоминают —
     * список будущих отказов в рантайме образа.
     * @param types Классы, которые создаются рефлексией
     * @return Имена классов, не объявленных в подсказках
     */
    public List<String> gaps(final Collection<Class<?>> types) {
        return types.stream()
            .filter(type -> !this.covers(type))
            .map(Class::getName)
            .toList();
    }
}

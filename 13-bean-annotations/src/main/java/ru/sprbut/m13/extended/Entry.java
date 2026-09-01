/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m13.extended;

import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Строка отчёта об одном бине.
 *
 * <p>Поле {@code instantiated} — самое полезное из шести: разница между
 * «бин объявлен» и «бин создан» на слайдах не видна вовсе, а в жизни именно
 * она объясняет, почему ошибка конфигурации всплывает не на старте.</p>
 *
 * @param name         имя бина в контейнере
 * @param type         тип бина
 * @param scope        область видимости
 * @param primary      помечен ли {@code @Primary}
 * @param lazy         помечен ли {@code @Lazy}
 * @param instantiated создан ли уже экземпляр
 * @param dependsOn    бины из {@code @DependsOn}
 * @since 1.0
 */
public record Entry(
    String name,
    String type,
    String scope,
    boolean primary,
    boolean lazy,
    boolean instantiated,
    List<String> dependsOn
) {

    /**
     * Значение {@code Entry}.
     */
    public Entry {
        dependsOn = List.copyOf(dependsOn);
    }

    /**
     * Синглтон ли это. Пустая строка тоже означает singleton — так контейнер
     * записывает область видимости по умолчанию.
     */
    public boolean singleton() {
        return BeanDefinition.SCOPE_SINGLETON.equals(this.scope) || this.scope.isEmpty();
    }
}

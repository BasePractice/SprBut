/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.lang.reflect.Constructor;

/**
 * Слайд 18: «Избыточность и мутабельность».
 *
 * <p>Бин, созданный конструктором без параметров, — то есть пустой и потому
 * заведомо невалидный. Между созданием и последним сеттером объект находится
 * в состоянии, которого предметная область не допускает, и с этим ничего
 * нельзя сделать: соглашение требует именно такого порядка.</p>
 *
 * <p>Это и есть главный аргумент в пользу {@code record} и билдеров из модуля 10.</p>
 *
 * @since 1.0
 */
public final class EmptyBean {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public EmptyBean(final Class<?> type) {
        this.type = type;
    }

    /**
     * Свежесозданный пустой экземпляр.
     * @return Свежесозданный пустой экземпляр
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object instance() {
        try {
            final Constructor<?> constructor = this.type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(
                String.format(
                    "Не удалось создать %s конструктором без параметров", this.type.getName()
                ),
                failure
            );
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m26.reflection;

/**
 * Слайд «AOT и native image»: динамическая загрузка класса по строке.
 *
 * <p>Три рефлективных действия подряд — {@code Class.forName}, поиск конструктора,
 * {@code newInstance} — и каждое требует отдельной подсказки для native image:
 * доступ к самому классу и доступ к его публичным конструкторам.</p>
 *
 * <p>Пока приложение живёт на JVM, платить за это не нужно: classloader найдёт
 * что угодно в classpath. Цена появляется ровно в момент перехода на native.</p>
 *
 * @since 1.0
 */
public final class PluginByName {

    /**
     * Имя класса расширения.
     */
    private final String type;

    /**
     * Основной конструктор.
     * @param type Имя класса расширения
     */
    public PluginByName(final String type) {
        this.type = type;
    }

    /**
     * Экземпляр расширения, созданный рефлексией по имени класса.
     * @return Расширение
     */
    public Plugin plugin() {
        try {
            return Class.forName(this.type)
                .asSubclass(Plugin.class)
                .getDeclaredConstructor()
                .newInstance();
        } catch (final ReflectiveOperationException failure) {
            throw new IllegalStateException(
                String.format("Расширение %s недоступно в этом рантайме", this.type),
                failure
            );
        }
    }
}

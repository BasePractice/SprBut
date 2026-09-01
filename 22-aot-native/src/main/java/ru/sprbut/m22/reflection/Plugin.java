/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m22.reflection;

/**
 * Расширение, имя класса которого приложение узнаёт только из конфигурации.
 *
 * <p>Именно такие места и ломаются в native image: компилятор GraalVM обходит граф
 * достижимости от точки входа, а класс, названный строкой в yaml, ни из одной
 * ссылки не достижим — и в образ просто не попадает.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Plugin {

    /**
     * Имя расширения для отчёта.
     * @return Имя расширения
     */
    String name();
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import ru.sprbut.m03.MethodSignature;

/**
 * <b>Расширенный пример модуля 03.</b>
 *
 * <p>Мини-движок команд: строка вида
 * <pre>ru.sprbut.m03.model.Order(A-1,100)#addLines(10,20)</pre>
 * превращается в реальный вызов метода. Внутри задействованы <i>все</i> узлы
 * карты Reflection API (СХЕМА 1, слайд 27) сразу:
 * <ul>
 * <li>{@code Class} — загрузка типа по имени через {@code Class.forName};</li>
 * <li>{@code Constructor} — выбор конструктора по количеству и типам аргументов;</li>
 * <li>{@code Method} — поиск метода, разбор сигнатуры, поддержка varargs;</li>
 * <li>{@code Parameter} — типы, по которым конвертируются строковые аргументы;</li>
 * <li>{@code Modifier} — отсев недоступных и абстрактных членов;</li>
 * <li>{@code Array} — упаковка хвоста аргументов в varargs-массив.</li>
 * </ul>
 * Это ровно тот механизм, на котором работают {@code spring-shell}, операции JMX
 * и маршрутизация запросов в Spring MVC: HTTP-запрос тоже приходит строкой,
 * а метод контроллера принимает типизированные аргументы.</p>
 *
 * @since 1.0
 */
public final class Command {

    /**
     * Текст.
     */
    private final String text;

    /**
     * Основной конструктор.
     * @param text Текст
     */
    public Command(final String text) {
        this.text = text;
    }

    /**
     * Результат выполнения команды.
     * @return Результат выполнения команды
     */
    public Invocation invocation() {
        final int hash = this.text.indexOf('#');
        if (hash < 0) {
            throw new IllegalArgumentException(
                "Ожидался формат 'Класс#метод(...)', получено: " + this.text
            );
        }
        final Spec target = new Spec(this.text.substring(0, hash).trim());
        final Spec call = new Spec(this.text.substring(hash + 1).trim());
        final Class<?> type = this.type(target.name());
        final ChosenConstructor chosen = new ChosenConstructor(type, target.args().size());
        final ChosenMethod method = new ChosenMethod(type, call.name(), call.args());
        return new Invocation(
            type.getSimpleName(),
            chosen.text(),
            new MethodSignature(method.method()).text(),
            method.result(chosen.instance(target.args()))
        );
    }

    private Class<?> type(final String name) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException absent) {
            throw new IllegalArgumentException("Класс не найден: " + name, absent);
        }
    }
}

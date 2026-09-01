/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 36: {@code Parameter} и {@code Executable}.
 *
 * <p>Один код для метода и конструктора — потому что оба являются {@link Executable}.
 * Так контейнер единообразно разбирает и точки внедрения через конструктор,
 * и через метод-сеттер: с его стороны разницы нет.</p>
 *
 * @since 1.0
 */
public final class Parameters {

    /**
     * Значение {@code executable}.
     */
    private final Executable executable;

    /**
     * Основной конструктор.
     * @param executable Значение {@code executable}
     */
    public Parameters(final Executable executable) {
        this.executable = executable;
    }

    /**
     * Описания параметров в виде «тип имя».
     * @return Описания параметров в виде «тип имя»
     */
    public List<String> descriptions() {
        return Arrays.stream(this.executable.getParameters())
            .map(parameter -> parameter.getType().getSimpleName() + " " + parameter.getName())
            .toList();
    }

    /**
     * Сохранены ли настоящие имена параметров. Без флага {@code -parameters}
     * здесь будут {@code arg0}, {@code arg1}, и квалификаторы по имени
     * перестают работать.
     * @return Сохранены ли настоящие имена параметров. Без флага {@code -parameters} здесь будут {@code arg0}, {@code arg1}, и квалификаторы по имени перестают работать
     */
    public boolean named() {
        return Arrays.stream(this.executable.getParameters())
            .allMatch(Parameter::isNamePresent);
    }

    /**
     * Точки внедрения — параметры, помеченные {@link Injected}. Имя из аннотации
     * служит квалификатором; если оно пустое, подбор идёт по имени параметра.
     * @return Точки внедрения — параметры, помеченные {@link Injected}. Имя из аннотации служит квалификатором; если оно пустое, подбор идёт по имени параметра
     */
    public List<String> injectionPoints() {
        return Arrays.stream(this.executable.getParameters())
            .filter(parameter -> parameter.isAnnotationPresent(Injected.class))
            .map(this::qualifier)
            .toList();
    }

    /**
     * Синтетические параметры компилятор добавляет сам — например, ссылку
     * на внешний объект во внутреннем классе.
     * @return Синтетические параметры компилятор добавляет сам — например, ссылку на внешний объект во внутреннем классе
     */
    public List<String> synthetic() {
        return Arrays.stream(this.executable.getParameters())
            .filter(Parameter::isSynthetic)
            .map(Parameter::getName)
            .toList();
    }

    private String qualifier(final Parameter parameter) {
        final String named = parameter.getAnnotation(Injected.class).value();
        return named.isBlank() ? parameter.getName() : named;
    }
}

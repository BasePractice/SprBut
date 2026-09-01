/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайды 12–16: подчиняется ли класс соглашению JavaBeans.
 *
 * <p>Проверка написана на рефлексии из модуля 01 — тем же способом, каким Spring
 * и Hibernate решают, умеют ли они работать с типом.</p>
 *
 * <p>Требование {@link Serializable} необязательно: слайд прямо оговаривает,
 * что Spring его не спрашивает. Поэтому строгость вынесена в конструктор —
 * один и тот же класс отвечает на оба вопроса.</p>
 *
 * @since 1.0
 */
public final class BeanVerdict {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Признак сериализуемости.
     */
    private final boolean serializable;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public BeanVerdict(final Class<?> type) {
        this(type, false);
    }

    /**
     * Основной конструктор.
     * @param type Тип
     * @param serializable Признак сериализуемости
     */
    public BeanVerdict(final Class<?> type, final boolean serializable) {
        this.type = type;
        this.serializable = serializable;
    }

    /**
     * Подчиняется ли класс соглашению.
     * @return Подчиняется ли класс соглашению
     */
    public boolean valid() {
        return this.violations().isEmpty();
    }

    /**
     * Нарушения соглашения, каждое одним предложением.
     * @return Нарушения соглашения, каждое одним предложением
     */
    public List<String> violations() {
        final List<String> found = new ArrayList<>();
        if (!this.constructible()) {
            found.add("нет публичного конструктора без параметров");
        }
        final BeanProperties properties = new BeanProperties(this.type);
        for (String property : properties.writable()) {
            if (properties.reader(property) == null) {
                found.add("у свойства '" + property + "' есть setter, но нет getter");
            }
        }
        if (this.serializable && !Serializable.class.isAssignableFrom(this.type)) {
            found.add("класс не реализует Serializable");
        }
        return List.copyOf(found);
    }

    /**
     * Первое требование соглашения: {@code public Xxx()}. Без него контейнер
     * не сможет создать объект дефолтным способом.
     * @return Первое требование соглашения: {@code public Xxx()}. Без него контейнер не сможет создать объект дефолтным способом
     */
    public boolean constructible() {
        for (Constructor<?> candidate : this.type.getConstructors()) {
            if (candidate.getParameterCount() == 0 && Modifier.isPublic(candidate.getModifiers())) {
                return true;
            }
        }
        return false;
    }
}

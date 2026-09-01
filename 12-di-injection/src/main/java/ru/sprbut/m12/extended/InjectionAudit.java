/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.extended;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextAware;

/**
 * <b>Расширенный пример модуля 12.</b>
 *
 * <p>Аудитор точек внедрения: разбирает класс рефлексией и выносит вердикт о том,
 * как в нём организованы зависимости. Каждое правило взято прямо со слайдов,
 * но здесь оно становится проверяемым:
 * <ul>
 * <li><b>конструктор предпочтителен</b> (слайд 92) — поля {@code final},
 * зависимости обязательны;</li>
 * <li><b>внедрение в поле мешает тестам</b> (слайд 93) — можно ли собрать
 * объект обычным {@code new};</li>
 * <li><b>Service Locator — антипаттерн</b> (слайд 95) — ловится по
 * {@code ApplicationContextAware};</li>
 * <li>слишком много зависимостей — сигнал, что класс делает слишком много.</li>
 * </ul>
 * Такую проверку не грех повесить в архитектурный тест проекта: она находит
 * ровно те классы, которые потом невозможно протестировать.</p>
 *
 * @since 1.0
 */
public final class InjectionAudit {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Предел.
     */
    private final int limit;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public InjectionAudit(final Class<?> type) {
        this(type, 5);
    }

    /**
     * Основной конструктор.
     * @param type Тип
     * @param limit Предел
     */
    public InjectionAudit(final Class<?> type, final int limit) {
        this.type = type;
        this.limit = limit;
    }

    /**
     * Вердикт по классу.
     * @return Вердикт по классу
     */
    public Report report() {
        final InjectionPoints points = new InjectionPoints(this.type);
        final List<Style> styles = new ArrayList<>();
        final List<String> dependencies = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final Constructor<?> injectable = points.constructor();
        if (injectable != null && injectable.getParameterCount() > 0) {
            styles.add(Style.CONSTRUCTOR);
            Arrays.stream(injectable.getParameterTypes())
                .map(Class::getSimpleName)
                .forEach(dependencies::add);
        }
        final List<Field> fields = points.fields();
        if (!fields.isEmpty()) {
            styles.add(Style.FIELD);
            fields.stream().map(field -> field.getType().getSimpleName()).forEach(dependencies::add);
            warnings.add(
                "внедрение в поле: класс нельзя собрать обычным new — поля "
                    + fields.stream().map(Field::getName).toList()
            );
        }
        final List<Method> setters = points.setters();
        if (!setters.isEmpty()) {
            styles.add(Style.SETTER);
            setters.stream()
                .map(setter -> setter.getParameterTypes()[0].getSimpleName())
                .forEach(dependencies::add);
        }
        final boolean locator = ApplicationContextAware.class.isAssignableFrom(this.type);
        if (locator) {
            styles.add(Style.SERVICE_LOCATOR);
            warnings.add(
                "Service Locator: класс сам ходит за зависимостями в контейнер — "
                    + "они не видны в API и не подменяются в тесте"
            );
        }
        if (styles.contains(Style.CONSTRUCTOR) && !points.immutable()
            && fields.isEmpty() && setters.isEmpty()) {
            warnings.add(
                "зависимости внедрены конструктором, но поля не final — "
                    + "их всё ещё можно переприсвоить"
            );
        }
        if (dependencies.size() > this.limit) {
            warnings.add(
                "зависимостей " + dependencies.size() + " — вероятно, класс делает слишком много"
            );
        }
        if (styles.size() > 1 && !locator) {
            warnings.add("смешаны способы внедрения: " + styles);
        }
        return new Report(
            this.type,
            styles,
            dependencies,
            styles.contains(Style.CONSTRUCTOR) && fields.isEmpty() && !locator,
            points.immutable(),
            warnings
        );
    }
}

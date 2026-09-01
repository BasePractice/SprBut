/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Метод реализации, соответствующий методу интерфейса.
 *
 * <p>Аннотации ищутся на методе <b>реализации</b>: у интерфейса их может не быть
 * вовсе. Ровно так же поступает Spring, разбирая {@code @Transactional} —
 * и ровно об этом забывают, когда вешают аннотацию на интерфейс и удивляются
 * тишине.</p>
 *
 * @since 1.0
 */
public final class TargetMethod {

    /**
     * Целевой объект.
     */
    private final Object target;

    /**
     * Объявленные элементы.
     */
    private final Method declared;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     * @param declared Объявленные элементы
     */
    public TargetMethod(final Object target, final Method declared) {
        this.target = target;
        this.declared = declared;
    }

    /**
     * Метод реализации; если его нет — метод интерфейса как есть.
     * @return Метод реализации; если его нет — метод интерфейса как есть
     */
    public Method method() {
        try {
            return this.target.getClass()
                .getMethod(this.declared.getName(), this.declared.getParameterTypes());
        } catch (final NoSuchMethodException absent) {
            return this.declared;
        }
    }

    /**
     * Хэндл для быстрого вызова: доступ проверяется один раз, при создании,
     * а не на каждом вызове, как у {@code Method.invoke}.
     * @return Хэндл для быстрого вызова: доступ проверяется один раз, при создании, а не на каждом вызове, как у {@code Method.invoke}
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public MethodHandle handle() {
        try {
            final Method implementation = this.method();
            implementation.setAccessible(true);
            return MethodHandles.lookup().unreflect(implementation);
        } catch (final ReflectiveOperationException denied) {
            throw new IllegalStateException(
                String.format("Не удалось получить хэндл для %s", this.declared.getName(), denied)
            );
        }
    }
}

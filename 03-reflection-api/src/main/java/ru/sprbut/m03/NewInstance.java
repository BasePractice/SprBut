/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Объект, созданный рефлексией под заданные аргументы.
 *
 * <p>Три разных отказа разведены намеренно: «нет подходящего конструктора»,
 * «тип нельзя инстанцировать» и «конструктор сам бросил исключение» —
 * это три разные ошибки, и общее сообщение про «не удалось создать»
 * не помогло бы ни в одном из случаев.</p>
 *
 * @since 1.0
 */
public final class NewInstance {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Аргументы.
     */
    private final Object[] args;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param args Аргументы
     */
    public NewInstance(final Class<?> type, final Object... args) {
        this.type = type;
        this.args = args.clone();
    }

    /**
     * Созданный объект.
     * @return Созданный объект
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object object() {
        final Constructor<?> chosen = new Constructors(this.type).matching(this.args)
            .orElseThrow(() -> new IllegalArgumentException(
                "Нет конструктора " + this.type.getSimpleName()
                    + " под аргументы " + Arrays.toString(this.args
)
            ));
        chosen.setAccessible(true);
        try {
            return chosen.newInstance(this.args);
        } catch (final InstantiationException abstractType) {
            throw new IllegalStateException(
                "Нельзя создать экземпляр " + this.type.getSimpleName()
                    + " — абстрактный класс или интерфейс",
                abstractType
            );
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к конструктору", denied);
        } catch (final InvocationTargetException wrapped) {
            final Throwable cause = wrapped.getCause();
            if (cause instanceof RuntimeException unchecked) {
                throw unchecked;
            }
            throw new IllegalStateException(cause);
        }
    }
}

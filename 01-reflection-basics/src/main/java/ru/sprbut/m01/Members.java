/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 6: «Определить модификаторы доступа полей, методов».
 *
 * <p>Члены одного класса, отобранные по модификаторам. Главное различие, которое
 * стоит запомнить раз и навсегда: {@code getDeclaredXxx} видит {@code private},
 * но только в самом классе, а {@code getXxx} — только {@code public},
 * зато вместе с унаследованными.</p>
 *
 * <p>Синтетические члены отфильтрованы везде: компилятор добавляет их сам
 * (мосты дженериков, ссылки на внешний класс), и в отчёте о коде,
 * который писал человек, им не место.</p>
 *
 * @since 1.0
 */
public final class Members {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Members(final Class<?> type) {
        this.type = type;
    }

    /**
     * Имена полей, объявленных прямо в этом классе, включая приватные.
     * @return Имена полей, объявленных прямо в этом классе, включая приватные
     */
    public List<String> declaredFields() {
        return this.names(Arrays.stream(this.type.getDeclaredFields()));
    }

    /**
     * Только публичные поля — включая унаследованные от родителей.
     * @return Только публичные поля — включая унаследованные от родителей
     */
    public List<String> publicFields() {
        return this.names(Arrays.stream(this.type.getFields()));
    }

    /**
     * Приватные поля этого класса — те, к которым нет доступа без рефлексии.
     * @return Приватные поля этого класса — те, к которым нет доступа без рефлексии
     */
    public List<String> privateFields() {
        return this.names(
            Arrays.stream(this.type.getDeclaredFields())
                .filter(field -> Modifier.isPrivate(field.getModifiers()))
        );
    }

    /**
     * Статические поля — их значение читается без экземпляра.
     * @return Статические поля — их значение читается без экземпляра
     */
    public List<String> staticFields() {
        return this.names(
            Arrays.stream(this.type.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
        );
    }

    /**
     * Имена методов, объявленных в этом классе, в алфавитном порядке.
     * @return Имена методов, объявленных в этом классе, в алфавитном порядке
     */
    public List<String> declaredMethods() {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(method -> !method.isSynthetic())
            .map(Method::getName)
            .sorted()
            .toList();
    }

    private List<String> names(final java.util.stream.Stream<Field> fields) {
        return fields
            .filter(field -> !field.isSynthetic())
            .map(Field::getName)
            .toList();
    }
}

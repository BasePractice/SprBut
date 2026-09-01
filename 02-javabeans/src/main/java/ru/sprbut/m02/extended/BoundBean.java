/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.extended;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.sprbut.m02.classic.BeanVerdict;
import ru.sprbut.m02.classic.EmptyBean;
import ru.sprbut.m02.classic.Introspected;
import ru.sprbut.m02.classic.Invoked;
import ru.sprbut.m02.classic.PropertyKey;

/**
 * <b>Расширенный пример модуля 02.</b>
 *
 * <p>Мини-биндер конфигурации: заполняет JavaBean из плоской карты «ключ → строка»,
 * ровно как {@code @ConfigurationProperties} заполняет объект из
 * {@code application.yaml}. Именно ради этого сценария соглашение JavaBeans
 * и существует — контейнеру нужен конструктор без параметров, чтобы создать
 * объект, и сеттеры, чтобы его наполнить.</p>
 *
 * <p>Заодно виден предел применимости соглашения: на {@code record} этот биндер
 * не работает вовсе — нет ни конструктора без параметров, ни сеттеров. Поэтому
 * Spring Boot для неизменяемых конфигураций пришлось учить отдельному режиму
 * constructor binding — тот самый, что используется в модуле 16.</p>
 *
 * @since 1.0
 */
public final class BoundBean<T> {

    /**
     * Тип.
     */
    private final Class<T> type;

    /**
     * Значения.
     */
    private final Map<String, String> values;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param values Значения
     */
    public BoundBean(final Class<T> type, final Map<String, String> values) {
        this.type = type;
        this.values = Map.copyOf(values);
    }

    /**
     * Заполненный объект вместе с отчётом о привязке.
     * @throws IllegalArgumentException если класс не подчиняется соглашению JavaBeans
     * @return Заполненный объект вместе с отчётом о привязке
     */
    @SuppressWarnings("unchecked")
    public Binding<T> result() {
        final BeanVerdict verdict = new BeanVerdict(this.type);
        if (!verdict.valid()) {
            throw new IllegalArgumentException(
                this.type.getSimpleName() + " не является JavaBean: "
                    + String.join("; ", verdict.violations())
            );
        }
        final T bean = (T) new EmptyBean(this.type).instance();
        final Introspected introspected = new Introspected(this.type);
        final List<String> bound = new ArrayList<>();
        final List<String> ignored = new ArrayList<>();
        for (final Map.Entry<String, String> entry : this.values.entrySet()) {
            final String property = new PropertyKey(entry.getKey()).camelCase();
            final PropertyDescriptor described = introspected.descriptor(property).orElse(null);
            if (described == null || described.getWriteMethod() == null) {
                ignored.add(entry.getKey());
                continue;
            }
            new Invoked(described.getWriteMethod(), bean).value(
                new Converted(entry.getValue(), described.getPropertyType(), property).value()
            );
            bound.add(property);
        }
        bound.sort(String::compareTo);
        ignored.sort(String::compareTo);
        return new Binding<>(bean, bound, ignored);
    }
}

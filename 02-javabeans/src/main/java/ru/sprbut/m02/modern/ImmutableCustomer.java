/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.modern;

import java.util.List;
import java.util.Objects;

/**
 * Слайд 19: неизменяемый объект + Builder.
 *
 * <p>Отличие от record: здесь можно контролировать защитное копирование коллекций
 * и собирать объект по частям, не теряя при этом неизменяемости результата.
 * Builder решает проблему «конструктор на 8 параметров, порядок которых
 * невозможно запомнить», не возвращая при этом мутабельность самому объекту.</p>
 *
 * @since 1.0
 */
public final class ImmutableCustomer {

    /**
     * Идентификатор.
     */
    private final String id;
    /**
     * Имя.
     */
    private final String firstName;
    /**
     * Имя.
     */
    private final String lastName;
    /**
     * Возраст.
     */
    private final int age;
    /**
     * Признак привилегированного клиента.
     */
    private final boolean vip;
    /**
     * Метки.
     */
    private final List<String> tags;

    private ImmutableCustomer(final Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id обязателен");
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.age = builder.age;
        this.vip = builder.vip;
        // Защитная копия: снаружи изменить список после сборки уже нельзя
        this.tags = List.copyOf(builder.tags);
    }

    /**
     * Значение {@code builder}.
     * @return Значение {@code builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * «Изменение» неизменяемого объекта: builder, предзаполненный текущими значениями.
     * @return «Изменение» неизменяемого объекта: builder, предзаполненный текущими значениями
     */
    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .age(this.age)
                .vip(this.vip)
                .tags(this.tags);
    }

    /**
     * Значение свойства {@code id}.
     * @return Значение свойства {@code id}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Значение: имя.
     * @return Значение: имя
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Значение: фамилия.
     * @return Значение: фамилия
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Значение свойства {@code age}.
     * @return Значение свойства {@code age}
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Значение: признак привилегированного клиента.
     * @return Значение: признак привилегированного клиента
     */
    public boolean isVip() {
        return this.vip;
    }

    /**
     * Значение: метки.
     * @return Значение: метки
     */
    public List<String> getTags() {
        return this.tags;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableCustomer other)) {
            return false;
        }
        return this.age == other.age && this.vip == other.vip
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.firstName, other.firstName)
                && Objects.equals(this.lastName, other.lastName)
                && Objects.equals(this.tags, other.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.age, this.vip, this.tags);
    }

    @Override
    public String toString() {
        return "ImmutableCustomer{this.id=" + this.id + ", fullName=" + this.firstName + " " + this.lastName + "}";
    }

    /**
     * Классический Builder: изменяемый только он, результат — неизменяем.
     * @since 1.0
     */
    public static final class Builder {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Builder() {
            // нечего инициализировать
        }

        /**
         * Идентификатор.
         */
        private String id;
        /**
         * Имя.
         */
        private String firstName;
        /**
         * Имя.
         */
        private String lastName;
        /**
         * Возраст.
         */
        private int age;
        /**
         * Признак привилегированного клиента.
         */
        private boolean vip;
        /**
         * Метки.
         */
        private List<String> tags = List.of();

        /**
         * Идентификатор.
         * @param id Идентификатор
         * @return Идентификатор
         */
        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * Имя.
         * @param firstName Имя
         * @return Имя
         */
        public Builder firstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * Имя.
         * @param lastName Имя
         * @return Имя
         */
        public Builder lastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * Возраст.
         * @param age Возраст
         * @return Возраст
         */
        public Builder age(final int age) {
            if (age < 0) {
                throw new IllegalArgumentException("Возраст не может быть отрицательным: " + age);
            }
            this.age = age;
            return this;
        }

        /**
         * Признак привилегированного клиента.
         * @param vip Признак привилегированного клиента
         * @return Признак привилегированного клиента
         */
        public Builder vip(final boolean vip) {
            this.vip = vip;
            return this;
        }

        /**
         * Метки.
         * @param tags Метки
         * @return Метки
         */
        public Builder tags(final List<String> tags) {
            this.tags = tags == null ? List.of() : tags;
            return this;
        }

        /**
         * Значение {@code build}.
         * @return Значение {@code build}
         */
        public ImmutableCustomer build() {
            return new ImmutableCustomer(this);
        }
    }
}

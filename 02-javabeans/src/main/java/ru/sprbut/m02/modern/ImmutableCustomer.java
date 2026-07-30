package ru.sprbut.m02.modern;

import java.util.List;
import java.util.Objects;

/**
 * Слайд 19: неизменяемый объект + Builder.
 * <p>
 * Отличие от record: здесь можно контролировать защитное копирование коллекций
 * и собирать объект по частям, не теряя при этом неизменяемости результата.
 * Builder решает проблему «конструктор на 8 параметров, порядок которых
 * невозможно запомнить», не возвращая при этом мутабельность самому объекту.
 */
public final class ImmutableCustomer {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final int age;
    private final boolean vip;
    private final List<String> tags;

    private ImmutableCustomer(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id обязателен");
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.age = builder.age;
        this.vip = builder.vip;
        // Защитная копия: снаружи изменить список после сборки уже нельзя
        this.tags = List.copyOf(builder.tags);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * «Изменение» неизменяемого объекта: builder, предзаполненный текущими значениями.
     */
    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .vip(vip)
                .tags(tags);
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public boolean isVip() {
        return vip;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableCustomer other)) {
            return false;
        }
        return age == other.age && vip == other.vip
                && Objects.equals(id, other.id)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(tags, other.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, age, vip, tags);
    }

    @Override
    public String toString() {
        return "ImmutableCustomer{id=" + id + ", fullName=" + firstName + " " + lastName + "}";
    }

    /**
     * Классический Builder: изменяемый только он, результат — неизменяем.
     */
    public static final class Builder {

        private String id;
        private String firstName;
        private String lastName;
        private int age;
        private boolean vip;
        private List<String> tags = List.of();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder age(int age) {
            if (age < 0) {
                throw new IllegalArgumentException("Возраст не может быть отрицательным: " + age);
            }
            this.age = age;
            return this;
        }

        public Builder vip(boolean vip) {
            this.vip = vip;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags == null ? List.of() : tags;
            return this;
        }

        public ImmutableCustomer build() {
            return new ImmutableCustomer(this);
        }
    }
}

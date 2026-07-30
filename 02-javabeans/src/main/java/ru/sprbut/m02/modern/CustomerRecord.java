package ru.sprbut.m02.modern;

/**
 * Слайд 19: «Lombok, record, Immutability (Builder)» — ответ на избыточность
 * и мутабельность классического бина.
 * <p>
 * {@code record} даёт неизменяемость, {@code equals}/{@code hashCode}/{@code toString}
 * и компактный конструктор для валидации — в пяти строках вместо шестидесяти.
 * <p>
 * Цена: record <b>не является</b> JavaBean. У него нет конструктора без параметров,
 * а аксессоры называются {@code firstName()}, а не {@code getFirstName()}. Поэтому
 * {@link java.beans.Introspector} не увидит у него ни одного свойства, и
 * старый код, рассчитанный на соглашение, с record работать не будет.
 */
public record CustomerRecord(String id, String firstName, String lastName, int age, boolean vip) {

    /** Компактный конструктор: валидация выполняется один раз, при создании. */
    public CustomerRecord {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id обязателен");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным: " + age);
        }
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    /**
     * Изменение состояния у неизменяемого объекта — это создание нового.
     */
    public CustomerRecord withVip(boolean newVip) {
        return new CustomerRecord(id, firstName, lastName, age, newVip);
    }
}

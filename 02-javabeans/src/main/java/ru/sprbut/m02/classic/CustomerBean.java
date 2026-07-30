package ru.sprbut.m02.classic;

import java.io.Serializable;
import java.util.Objects;

/**
 * Слайды 12–17: классический JavaBean по всем правилам соглашения.
 * <ul>
 *   <li>публичный конструктор без параметров;</li>
 *   <li>свойства доступны через getter/setter;</li>
 *   <li>реализует {@link Serializable} (Spring этого не требует, но соглашение — да).</li>
 * </ul>
 * <p>
 * Обратная сторона, о которой говорит слайд 18: <b>избыточность и мутабельность</b>.
 * Пять свойств — это ~60 строк шаблонного кода, и объект в любой момент можно
 * привести в невалидное состояние: он создаётся пустым и заполняется по частям.
 */
public class CustomerBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;
    private int age;
    private boolean vip;

    /** Публичный конструктор без параметров — обязательное требование соглашения. */
    public CustomerBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Для boolean соглашение разрешает префикс {@code is} вместо {@code get}.
     * {@link java.beans.Introspector} понимает оба варианта.
     */
    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    /**
     * Вычисляемое свойство: геттер без поля. Introspector всё равно увидит
     * свойство {@code fullName} — свойство определяется методами, а не полями.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerBean other)) {
            return false;
        }
        return age == other.age
                && vip == other.vip
                && Objects.equals(id, other.id)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, age, vip);
    }

    @Override
    public String toString() {
        return "CustomerBean{id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
                + ", age=" + age + ", vip=" + vip + "}";
    }
}

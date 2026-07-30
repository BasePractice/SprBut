package ru.sprbut.m08.model;

import ru.sprbut.m07.api.GenerateBuilder;

import java.math.BigDecimal;

/**
 * Обычный JavaBean, помеченный {@code @GenerateBuilder} (модуль 07).
 * <p>
 * Никакого билдера здесь не написано — он появится в
 * {@code target/generated-sources/annotations} на этапе компиляции,
 * и им можно пользоваться из обычного кода, как будто он написан руками.
 */
@GenerateBuilder
public class Customer {

    private String id;
    private String name;
    private String email;
    private int age;
    private boolean vip;
    private BigDecimal balance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name=" + name + ", vip=" + vip + "}";
    }
}

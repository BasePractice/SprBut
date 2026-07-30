package ru.sprbut.m09.model;

import java.util.Objects;

/** Целевой объект маппинга. Поля {@code internalNote} здесь намеренно нет. */
public class UserDto {

    private String id;
    private String firstName;
    private String lastName;
    private int age;
    private boolean active;

    public UserDto() {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDto other)) {
            return false;
        }
        return age == other.age && active == other.active
                && Objects.equals(id, other.id)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, age, active);
    }

    @Override
    public String toString() {
        return "UserDto{id=" + id + ", firstName=" + firstName + ", age=" + age + "}";
    }
}

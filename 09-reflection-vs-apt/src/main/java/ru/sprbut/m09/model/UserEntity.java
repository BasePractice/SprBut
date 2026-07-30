package ru.sprbut.m09.model;

/**
 * Источник для маппинга. Один и тот же перенос данных в {@link UserDto}
 * реализован в модуле тремя способами — рефлексией, «как после APT» и байткодом.
 */
public class UserEntity {

    private String id;
    private String firstName;
    private String lastName;
    private int age;
    private boolean active;
    private String internalNote;

    public UserEntity() {
    }

    public UserEntity(String id, String firstName, String lastName, int age, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.active = active;
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

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }
}

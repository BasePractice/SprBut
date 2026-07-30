package ru.sprbut.m05.extended;

/**
 * Подопытный объект со всеми видами ограничений сразу.
 * <p>
 * Наследуется от {@link BaseEntity} намеренно: ограничения родителя обязаны
 * действовать, хотя {@code @Inherited} к полям отношения не имеет.
 */
@SuppressWarnings("unused")
public final class User extends BaseEntity {

    @NotBlank
    @MaxLength(10)
    private final String login;

    @Range(min = 18, max = 120, message = "возраст вне диапазона")
    private final int age;

    @Matches(regex = ".*@.*", message = "не похоже на почту")
    @Matches(regex = ".*\\.[a-z]+", message = "нет доменной зоны")
    private final String email;

    @InvisibleNotNull
    private final String invisible;

    private final String free;

    public User(String id, String login, int age, String email) {
        this(id, login, age, email, null, "без ограничений");
    }

    public User(String id, String login, int age, String email, String invisible, String free) {
        super(id);
        this.login = login;
        this.age = age;
        this.email = email;
        this.invisible = invisible;
        this.free = free;
    }
}

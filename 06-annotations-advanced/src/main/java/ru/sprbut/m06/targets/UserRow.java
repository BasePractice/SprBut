package ru.sprbut.m06.targets;

/**
 * Record, компоненты которого помечены отдельно от полей.
 *
 * @param id    идентификатор
 * @param login имя пользователя
 */
public record UserRow(@Column(name = "user_id") String id, @Column(name = "login") String login) {
}

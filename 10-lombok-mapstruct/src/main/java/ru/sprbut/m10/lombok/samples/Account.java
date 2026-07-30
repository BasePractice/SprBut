package ru.sprbut.m10.lombok.samples;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * {@code @ToString} и {@code @EqualsAndHashCode} с исключением полей.
 * <p>
 * Частый приём для сущностей: пароль не должен попадать в логи, а равенство
 * определяется идентификатором, а не всем состоянием сразу.
 */
@Getter
@RequiredArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class Account {

    private final String id;

    private final String login;

    private final String password;
}

package ru.sprbut.m06.samples;

import ru.sprbut.m06.web.RestController;

/**
 * Класс с композицией первого уровня: {@code @Controller} доступен через
 * {@code @RestController}, но не напрямую.
 */
@RestController("users")
public class UserApi {
}

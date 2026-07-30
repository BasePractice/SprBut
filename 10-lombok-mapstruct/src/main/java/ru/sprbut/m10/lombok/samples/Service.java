package ru.sprbut.m10.lombok.samples;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@code @RequiredArgsConstructor} — конструктор из {@code final} полей.
 * <p>
 * Это <b>основной способ</b> внедрения зависимостей через конструктор
 * в Spring-коде (модуль 12): полей два, конструктор пишется сам,
 * и добавление третьей зависимости не требует правки конструктора.
 */
@RequiredArgsConstructor
@Getter
public class Service {

    private final String name;

    private final int retries;

    private String mutableState = "меняется";
}

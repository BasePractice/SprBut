/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m26.versions;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд «Версии»: {@code jakarta.annotation.PostConstruct} вместо {@code javax}.
 *
 * <p>Аннотации те же с точностью до имени пакета, поведение то же — но код,
 * не переехавший на {@code jakarta}, Spring Boot 3 молча проигнорирует.
 * Метод не будет вызван, а ошибки не будет: аннотация из чужого пакета
 * для контейнера просто ничего не значит.</p>
 *
 * @since 1.0
 */
public final class Cache {

    /**
     * Журнал вызовов жизненного цикла.
     */
    private final List<String> events;

    /**
     * Вторичный конструктор с пустым журналом.
     */
    public Cache() {
        this(new ArrayList<>(2));
    }

    /**
     * Основной конструктор.
     * @param events Журнал вызовов жизненного цикла
     */
    public Cache(final List<String> events) {
        this.events = events;
    }

    /**
     * Прогрев кэша при старте.
     * Метод обращается к состоянию объекта, статическим быть не может.
     */
    @PostConstruct
    public void warm() {
        this.events.add("warm");
    }

    /**
     * Сброс кэша при остановке.
     */
    @PreDestroy
    public void flush() {
        this.events.add("flush");
    }

    /**
     * Журнал вызовов жизненного цикла.
     * @return Журнал вызовов
     */
    public List<String> events() {
        return List.copyOf(this.events);
    }
}

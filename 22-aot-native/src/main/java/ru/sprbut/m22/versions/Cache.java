package ru.sprbut.m22.versions;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд «Версии»: {@code jakarta.annotation.PostConstruct} вместо {@code javax}.
 * <p>
 * Аннотации те же с точностью до имени пакета, поведение то же — но код,
 * не переехавший на {@code jakarta}, Spring Boot 3 молча проигнорирует.
 * Метод не будет вызван, а ошибки не будет: аннотация из чужого пакета
 * для контейнера просто ничего не значит.
 */
public final class Cache {

    private final List<String> events;

    public Cache() {
        this(new ArrayList<>());
    }

    public Cache(List<String> events) {
        this.events = events;
    }

    @PostConstruct
    public void warm() {
        this.events.add("warm");
    }

    @PreDestroy
    public void flush() {
        this.events.add("flush");
    }

    /**
     * Журнал вызовов жизненного цикла.
     */
    public List<String> events() {
        return List.copyOf(this.events);
    }
}

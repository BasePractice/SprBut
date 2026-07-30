package ru.sprbut.m23.extended;

import java.util.List;

/**
 * Карточка одного бина: что это, как долго живёт и во что обёрнуто.
 * <p>
 * Четыре факта, которых достаточно, чтобы объяснить почти любое загадочное
 * поведение приложения: подменённая область видимости, потерянный
 * {@code @Transactional}, неперехваченный self-invocation.
 */
public final class BeanCard {

    private final String name;

    private final String type;

    private final String scope;

    private final List<String> audited;

    public BeanCard(String name, String type, String scope, List<String> audited) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.audited = List.copyOf(audited);
    }

    /**
     * Имя бина в контейнере.
     */
    public String name() {
        return this.name;
    }

    /**
     * Настоящий класс за прокси, если прокси есть.
     */
    public String type() {
        return this.type;
    }

    /**
     * Область видимости: singleton, prototype или иная.
     */
    public String scope() {
        return this.scope;
    }

    /**
     * Методы, помеченные аннотацией аудита.
     */
    public List<String> audited() {
        return this.audited;
    }
}

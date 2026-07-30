package ru.sprbut.m03.extended;

import java.util.Arrays;
import java.util.List;

/**
 * Разобранная часть команды: имя и список сырых аргументов.
 * <p>
 * Одинаково разбирает обе половины — {@code Класс(a,b)} и {@code метод(c,d)}:
 * синтаксис у них общий, и различать их до разбора незачем.
 */
public final class Spec {

    private final String raw;

    public Spec(String raw) {
        this.raw = raw;
    }

    /**
     * Имя до открывающей скобки.
     */
    public String name() {
        int open = this.raw.indexOf('(');
        if (open < 0) {
            return this.raw;
        }
        return this.raw.substring(0, open).trim();
    }

    /**
     * Аргументы между скобками, ещё строками.
     */
    public List<String> args() {
        int open = this.raw.indexOf('(');
        if (open < 0) {
            return List.of();
        }
        if (!this.raw.endsWith(")")) {
            throw new IllegalArgumentException("Не закрыта скобка в: " + this.raw);
        }
        String inside = this.raw.substring(open + 1, this.raw.length() - 1).trim();
        if (inside.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(inside.split(",")).map(String::trim).toList();
    }
}

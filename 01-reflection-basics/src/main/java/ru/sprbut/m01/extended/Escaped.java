package ru.sprbut.m01.extended;

/**
 * Строка, безопасная для вставки в JSON.
 * <p>
 * Кавычки и обратные слэши экранируются, переводы строк и табуляции
 * заменяются escape-последовательностями. Без этого любой текст
 * с кавычкой ломал бы весь документ.
 */
public final class Escaped {

    private final String raw;

    public Escaped(String raw) {
        this.raw = raw;
    }

    /**
     * Экранированный текст.
     */
    public String text() {
        return this.raw
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}

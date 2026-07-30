package ru.sprbut.m22.versions;

/**
 * Слайд «Версии»: правило переезда с Spring Boot 2 на 3.
 * <p>
 * Пакет {@code javax.*} стал {@code jakarta.*} — но не весь. Исключение,
 * на котором спотыкаются все: {@code javax.annotation.processing} остался
 * на месте, потому что принадлежит JDK, а не Java EE. Переехало то,
 * что Oracle передал фонду Eclipse.
 */
public final class Migration {

    private final String origin;

    public Migration(String origin) {
        this.origin = origin;
    }

    /**
     * Имя пакета после переезда на Jakarta EE 9.
     */
    public String target() {
        if (this.origin.startsWith("javax.annotation.processing")) {
            return this.origin;
        }
        if (this.origin.startsWith("javax.")) {
            return this.origin.replaceFirst("^javax\\.", "jakarta.");
        }
        return this.origin;
    }
}

package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Audited;

/**
 * Интерфейс с аннотацией. {@code @Inherited} на интерфейсы не распространяется
 * вовсе — это третья и самая неожиданная его граница.
 */
@Audited(actor = "интерфейс")
public interface AuditedContract {

    /**
     * Действие контракта.
     */
    String action();
}

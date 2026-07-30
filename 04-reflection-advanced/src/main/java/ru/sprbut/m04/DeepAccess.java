package ru.sprbut.m04;

import java.lang.reflect.Field;

/**
 * Слайд 31: «setAccessible и JPMS: нужен --add-opens».
 * <p>
 * Попытка снять проверку доступа с приватного поля чужого класса. Успех
 * зависит <b>не от модификатора поля</b>, а от того, открыт ли пакет —
 * и это самая неочевидная часть модульной системы.
 * <p>
 * В этом проекте флаги заданы в {@code surefire.argLine} корневого pom.xml:
 * открыты {@code java.lang}, {@code java.util} и {@code java.time}.
 * Пакет {@code java.io} не открыт, и на нём разница видна.
 */
public final class DeepAccess {

    private final Class<?> type;

    private final String field;

    public DeepAccess(Class<?> type, String field) {
        this.type = type;
        this.field = field;
    }

    /**
     * Результат попытки: успех либо имя и текст исключения.
     */
    public AccessAttempt attempt() {
        try {
            Field found = this.type.getDeclaredField(this.field);
            found.setAccessible(true);
            return AccessAttempt.ok();
        } catch (NoSuchFieldException absent) {
            return new AccessAttempt(false, "NoSuchFieldException", absent.getMessage());
        } catch (RuntimeException denied) {
            return new AccessAttempt(false, denied.getClass().getSimpleName(), denied.getMessage());
        }
    }
}

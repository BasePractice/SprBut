package ru.sprbut.m04;

import java.lang.reflect.Field;

/**
 * Слайд 31: «setAccessible и JPMS: нужен --add-opens».
 * <p>
 * Модульная система Java (JPMS) различает два уровня:
 * <ul>
 *   <li><b>exports</b> — типы пакета видны на этапе компиляции и в обычном коде;</li>
 *   <li><b>opens</b> — пакет дополнительно открыт для <i>глубокой рефлексии</i>,
 *       то есть {@code setAccessible(true)} на его закрытых членах разрешён.</li>
 * </ul>
 * Модуль {@code java.base} экспортирует почти всё, но открывает — почти ничего.
 * Поэтому Spring, Hibernate и Jackson требуют флагов вида
 * {@code --add-opens java.base/java.lang=ALL-UNNAMED}.
 * <p>
 * В этом проекте такие флаги заданы в {@code surefire.argLine} корневого pom.xml —
 * там открыты {@code java.lang}, {@code java.util} и {@code java.time}.
 * Пакет {@code java.io} <i>не</i> открыт, и на нём видна разница.
 */
public final class JpmsAccess {

    private JpmsAccess() {
    }

    /** Результат попытки открыть доступ к закрытому члену чужого модуля. */
    public record AccessAttempt(boolean succeeded, String failureType, String message) {

        public static AccessAttempt ok() {
            return new AccessAttempt(true, null, null);
        }
    }

    /**
     * Пытается снять проверку доступа с приватного поля указанного класса.
     * Успех зависит не от модификатора поля, а от того, открыт ли пакет.
     */
    public static AccessAttempt tryOpen(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return AccessAttempt.ok();
        } catch (NoSuchFieldException e) {
            return new AccessAttempt(false, "NoSuchFieldException", e.getMessage());
        } catch (RuntimeException e) {
            // InaccessibleObjectException — наследник RuntimeException, появился в Java 9
            return new AccessAttempt(false, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Открыт ли пакет чужого модуля для глубокой рефлексии из нашего.
     * Это штатный способ проверить доступ, не ловя исключение.
     */
    public static boolean isOpenToUs(Class<?> type) {
        Module owner = type.getModule();
        return owner.isOpen(type.getPackageName(), JpmsAccess.class.getModule());
    }

    /** Экспортирован ли пакет — то есть виден ли он обычному коду. */
    public static boolean isExportedToUs(Class<?> type) {
        Module owner = type.getModule();
        return owner.isExported(type.getPackageName(), JpmsAccess.class.getModule());
    }

    /**
     * Имя модуля, которому принадлежит класс. Код, запущенный с classpath,
     * попадает в безымянный модуль — у него {@code getName()} возвращает null.
     */
    public static String moduleNameOf(Class<?> type) {
        return type.getModule().getName();
    }

    /**
     * Свой класс из безымянного модуля открыт всегда — ограничения JPMS
     * касаются только границ между модулями.
     */
    @SuppressWarnings("unused")
    static class OurOwnClass {
        private String secret = "доступно";
    }
}

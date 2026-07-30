package ru.sprbut.m04;

/**
 * Итог попытки открыть доступ к закрытому члену.
 *
 * @param succeeded удалось ли снять проверку доступа
 * @param failure   имя класса исключения, если не удалось
 * @param message   сообщение исключения, если не удалось
 */
public record AccessAttempt(boolean succeeded, String failure, String message) {

    /**
     * Успешная попытка.
     */
    public static AccessAttempt ok() {
        return new AccessAttempt(true, null, null);
    }
}

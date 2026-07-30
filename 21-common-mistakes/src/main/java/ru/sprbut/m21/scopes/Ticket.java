package ru.sprbut.m21.scopes;

/**
 * Prototype-бин: контейнер обязан отдавать новый экземпляр на каждый запрос.
 * <p>
 * Класс намеренно не {@code final}. С {@code proxyMode = TARGET_CLASS} Spring
 * строит CGLIB-подкласс, а унаследоваться от финального класса нельзя —
 * это тот случай, когда требование фреймворка перевешивает привычку
 * закрывать классы от наследования.
 */
public class Ticket {

    private final int number;

    public Ticket(Serial serial) {
        this.number = serial.next();
    }

    /**
     * Номер, присвоенный при создании экземпляра.
     */
    public int number() {
        return this.number;
    }
}

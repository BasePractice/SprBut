package ru.sprbut.m21.scopes;

/**
 * Singleton, который держит в себе prototype-бин.
 * <p>
 * Здесь и прячется ошибка со слайда: зависимость внедряется <b>один раз</b>,
 * при создании singleton'а. Дальше «прототип» живёт столько же, сколько его
 * владелец, и {@code prototype} остаётся таковым только на бумаге.
 */
public final class Gate {

    private final Ticket ticket;

    public Gate(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Номер очередного посетителя.
     */
    public int admit() {
        return this.ticket.number();
    }
}

package ru.sprbut.m21.circular;

/**
 * Одна половина цикла: счета не умеют считать итог без книги проводок.
 */
public final class InvoiceService implements Invoices {

    private final Ledger ledger;

    public InvoiceService(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public int total() {
        return this.ledger.entries() * 100;
    }
}

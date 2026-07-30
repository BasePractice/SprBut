package ru.sprbut.m21.circular;

/**
 * Вторая половина цикла: книга проводок спрашивает счета о сумме.
 * <p>
 * Обращение к {@link Invoices} отложено до вызова {@link #entries()} — именно
 * поэтому {@code @Lazy}-прокси спасает положение: на этапе сборки контекста
 * настоящий объект ещё не нужен.
 */
public final class LedgerService implements Ledger {

    private final Invoices invoices;

    public LedgerService(Invoices invoices) {
        this.invoices = invoices;
    }

    @Override
    public int entries() {
        return 3;
    }

    /**
     * Сверка книги со счетами — здесь прокси наконец разворачивается в бин.
     */
    public boolean balanced() {
        return this.invoices.total() == entries() * 100;
    }
}

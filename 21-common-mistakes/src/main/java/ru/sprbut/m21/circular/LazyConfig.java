package ru.sprbut.m21.circular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Лечение цикла отсрочкой: {@code @Lazy} на одной из двух точек внедрения.
 * <p>
 * Вместо настоящего бина {@code ledger} получает JDK-прокси, который найдёт
 * {@link Invoices} в контейнере при первом вызове метода. К этому моменту
 * контекст уже собран, цикла нет.
 * <p>
 * Это обезболивающее, а не лечение: взаимная зависимость никуда не делась,
 * её просто перестало быть видно на старте. Честный выход — {@link SplitConfig}.
 */
@Configuration(proxyBeanMethods = false)
public final class LazyConfig {

    @Bean
    public Invoices invoices(Ledger ledger) {
        return new InvoiceService(ledger);
    }

    @Bean
    public Ledger ledger(@Lazy Invoices invoices) {
        return new LedgerService(invoices);
    }
}

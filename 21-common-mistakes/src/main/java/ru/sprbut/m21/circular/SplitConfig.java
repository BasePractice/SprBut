package ru.sprbut.m21.circular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Честное лечение цикла: разделить бины.
 * <p>
 * Взаимная зависимость почти всегда означает, что два класса делят одну
 * ответственность. Здесь она вынесена наружу — {@code ledger} больше не знает
 * о счетах, и цикл исчезает не потому, что его спрятали, а потому,
 * что его не стало.
 */
@Configuration(proxyBeanMethods = false)
public final class SplitConfig {

    @Bean
    public Ledger ledger() {
        return () -> 3;
    }

    @Bean
    public Invoices invoices(Ledger ledger) {
        return new InvoiceService(ledger);
    }
}

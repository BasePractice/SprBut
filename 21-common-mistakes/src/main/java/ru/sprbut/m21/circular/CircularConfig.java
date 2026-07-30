package ru.sprbut.m21.circular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Слайд «Типичные ошибки»: circular reference и {@code BeanCurrentlyInCreationException}.
 * <p>
 * Два бина требуют друг друга через конструктор. Контейнер начинает собирать
 * {@code invoices}, обнаруживает зависимость от {@code ledger}, идёт собирать его,
 * снова упирается в недособранный {@code invoices} — и останавливается,
 * потому что отдать наполовину созданный объект он не может.
 * <p>
 * Через поля или сеттеры цикл бы «сработал»: Spring подставил бы недоинициализированную
 * ссылку. Конструктор делает проблему видимой на старте — это его достоинство,
 * а не недостаток. Начиная с Boot 2.6 циклы запрещены и по умолчанию.
 */
@Configuration(proxyBeanMethods = false)
public final class CircularConfig {

    @Bean
    public Invoices invoices(Ledger ledger) {
        return new InvoiceService(ledger);
    }

    @Bean
    public Ledger ledger(Invoices invoices) {
        return new LedgerService(invoices);
    }
}

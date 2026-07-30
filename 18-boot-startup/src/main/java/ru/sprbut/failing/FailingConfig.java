package ru.sprbut.failing;

import org.springframework.context.annotation.Bean;

/**
 * Конфигурация, которая намеренно роняет создание бина — чтобы увидеть
 * {@code ApplicationFailedEvent} вместо {@code ApplicationReadyEvent}.
 * <p>
 * Лежит <b>вне</b> пакета {@code ru.sprbut.m18} специально. Внутри него её
 * подхватило бы сканирование {@code @SpringBootApplication}: класс с
 * {@code @Bean}-методами считается «lite»-конфигурацией, даже если на нём нет
 * ни одного стереотипа. Тогда падал бы каждый запуск, а не только тот, где это нужно.
 */
public class FailingConfig {

    @Bean
    public String brokenBean() {
        throw new IllegalStateException("бин не создался");
    }
}

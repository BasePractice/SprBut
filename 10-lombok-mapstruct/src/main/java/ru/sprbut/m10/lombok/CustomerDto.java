package ru.sprbut.m10.lombok;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Lombok: {@code @Value} — неизменяемый аналог {@code @Data}.
 * <p>
 * Все поля становятся {@code private final}, класс — {@code final},
 * сеттеров нет, зато есть конструктор со всеми аргументами.
 * {@code @Builder} добавляет сборку по частям — вместе получается ровно то,
 * что в модуле 02 писалось руками на 130 строк.
 */
@Value
@Builder(toBuilder = true)
public class CustomerDto {

    String id;
    String fullName;
    int age;
    BigDecimal balance;
    String status;
}

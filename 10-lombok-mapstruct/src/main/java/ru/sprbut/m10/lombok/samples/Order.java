package ru.sprbut.m10.lombok.samples;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * {@code @Builder} с {@code @Singular}: коллекция наполняется по одному
 * элементу и становится неизменяемой в собранном объекте.
 * <p>
 * Именно это отличает билдер от JavaBeans-подхода: объект нельзя увидеть
 * недособранным, потому что до вызова {@code build()} его не существует.
 */
@Builder
@Getter
public class Order {

    private final String number;

    @Singular
    private final List<String> items;
}

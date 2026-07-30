package ru.sprbut.m15.aop;

import java.math.BigDecimal;

/**
 * Слайд 122: «JDK dynamic proxy — если есть интерфейс».
 * <p>
 * Практическое следствие: бин с интерфейсом попадает в контекст как JDK-прокси,
 * и получить его по классу реализации уже нельзя — только по интерфейсу.
 */
public interface DiscountService {

    BigDecimal calculate(BigDecimal amount);

    String name();
}

package ru.sprbut.m10.lombok;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lombok: {@code @Data} = {@code @Getter} + {@code @Setter} + {@code @ToString}
 * + {@code @EqualsAndHashCode} + {@code @RequiredArgsConstructor}.
 * <p>
 * Сравните с {@code CustomerBean} из модуля 02: там те же пять свойств заняли
 * шестьдесят строк. Здесь — три аннотации.
 * <p>
 * Механизм принципиально отличается от обычного APT (модуль 07). Штатное API
 * умеет только <b>создавать новые файлы</b>, а Lombok <b>меняет существующий
 * класс</b> — он лезет во внутренний AST javac (слайд 59, «хак AST»). Поэтому
 * методов нет в исходнике, но они есть в байткоде.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {

    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private BigDecimal balance;
    private boolean vip;
    private String internalNote;
}

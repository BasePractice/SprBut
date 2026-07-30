package ru.sprbut.m08.model;

import ru.sprbut.m07.api.GenerateBuilder;
import ru.sprbut.m07.api.Todo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Второй бин с генерацией билдера — плюс {@code @Todo}, чтобы в логе сборки
 * было видно работу процессора-анализатора (слайд 60).
 * <p>
 * Суффикс имени генерируемого класса переопределён: получится {@code OrderMaker}.
 */
@GenerateBuilder(suffix = "Maker")
public class Order {

    private String number;
    private String customerId;
    private BigDecimal total;
    private LocalDate placedOn;

    @Todo("перевести на enum статусов")
    private String status;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDate getPlacedOn() {
        return placedOn;
    }

    public void setPlacedOn(LocalDate placedOn) {
        this.placedOn = placedOn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package ru.sprbut.m05.samples;

/**
 * Реализация, до которой аннотация интерфейса не доходит.
 */
public class ContractImpl implements AuditedContract {

    @Override
    public String action() {
        return "impl";
    }
}

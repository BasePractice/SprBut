/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

/**
 * Реализация, до которой аннотация интерфейса не доходит.
 * @since 1.0
 */
public class ContractImpl implements AuditedContract {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ContractImpl() {
        // нечего инициализировать
    }

    @Override
    public final String action() {
        return "impl";
    }
}

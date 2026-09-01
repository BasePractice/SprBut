/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.ambiguous;

/**
 * Служба доставки. В контексте её реализаций будет две — этого достаточно,
 * чтобы контейнер перестал понимать, какую именно внедрять.
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Shipper {

    /**
     * Срок доставки в днях.
     * @return Срок доставки в днях
     */
    int days();
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14.extended;

/**
 * Шаг жизненного цикла: номер, название фазы и бин.
 * @param number порядковый номер шага со слайда 118
 * @param phase  название фазы
 * @param bean   имя бина
 * @since 1.0
 */
public record Step(int number, String phase, String bean) {

    @Override
    public String toString() {
        return String.format("%s. %s → %s", this.number, this.phase, this.bean);
    }
}

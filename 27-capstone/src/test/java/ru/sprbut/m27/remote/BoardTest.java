/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.remote;

import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sprbut.m27.audit.AuditTrail;
import ru.sprbut.m27.domain.Task;

/**
 * Сосед по сети: отказ как нормальный режим работы.
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Сосед по сети: отказ как нормальный режим работы")
final class BoardTest {

    /**
     * Доска соседа.
     */
    @Autowired
    private Board board;

    /**
     * Журнал событий.
     */
    @Autowired
    private AuditTrail trail;

    @Test
    @DisplayName("молчание соседа не доходит до вызывающего исключением")
    void dontThrowWhenNeighbourIsSilent() {
        this.board.announce(new Task("сообщить доске", Instant.parse("2026-07-30T10:00:00Z")));
        MatcherAssert.assertThat(
            "dead neighbour cannot leave the caller with an exception",
            this.trail.records(),
            Matchers.hasItem("board:offline")
        );
    }
}

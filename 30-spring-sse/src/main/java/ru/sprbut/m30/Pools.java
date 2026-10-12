/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Пулы потоков экземпляра из свойств {@code sse.pools.*}.
 *
 * <p>Доставка событий пользователям и обработка команд gRPC идут в разных
 * пулах: всплеск отправок не задерживает доставку, а медленная доставка
 * не мешает принимать новые команды.</p>
 *
 * @param delivery Пул доставки событий из каналов Redis в потоки пользователей
 * @param commands Пул обработки вызовов gRPC
 * @since 1.0
 */
@ConfigurationProperties("sse.pools")
public record Pools(@DefaultValue Pool delivery, @DefaultValue Pool commands) {
}

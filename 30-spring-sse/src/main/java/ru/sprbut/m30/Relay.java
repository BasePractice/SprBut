/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

/**
 * Подписка экземпляра на общий канал: всё пришедшее рассылается всем
 * его подключённым пользователям.
 *
 * <p>Подписка открывается вместе с контекстом Spring и живёт, пока жив
 * контекст. Событие, которое не удалось разослать, пропускается.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public final class Relay implements SmartLifecycle {

    /**
     * Каналы уведомлений.
     */
    private final Bus bus;

    /**
     * Подключённые пользователи.
     */
    private final Audience audience;

    /**
     * Текущая подписка на общий канал.
     */
    private final AtomicReference<Disposable> link;

    /**
     * Конструктор.
     * @param bus Каналы уведомлений
     * @param audience Подключённые пользователи
     */
    public Relay(final Bus bus, final Audience audience) {
        this(bus, audience, new AtomicReference<>(Disposables.disposed()));
    }

    @Override
    public void start() {
        this.link.getAndSet(
            this.bus.broadcasts()
                .concatMap(
                    event -> Mono.fromRunnable(() -> this.audience.broadcast(event))
                        .doOnError(
                            ex -> log.warn(
                                String.format(
                                    "Событие %s пропущено: его не удалось разослать",
                                    event.data()
                                ),
                                ex
                            )
                        )
                        .onErrorComplete()
                )
                .subscribe()
        ).dispose();
    }

    @Override
    public void stop() {
        this.link.getAndSet(Disposables.disposed()).dispose();
    }

    @Override
    public boolean isRunning() {
        return !this.link.get().isDisposed();
    }
}

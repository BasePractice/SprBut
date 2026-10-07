/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Подписка экземпляра на общий канал: всё пришедшее доставляется его пользователям.
 *
 * <p>Битое уведомление пропускается, обрыв канала лечится переподпиской,
 * поэтому подписка живёт, пока жив контекст Spring.</p>
 *
 * @since 1.0
 */
public final class Relay implements SmartLifecycle {

    /**
     * Журнал.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Relay.class);

    /**
     * Канал уведомлений.
     */
    private final Bus bus;

    /**
     * Подключённые пользователи.
     */
    private final Audience audience;

    /**
     * Пауза перед первой переподпиской.
     */
    private final Duration pause;

    /**
     * Текущая подписка на канал.
     */
    private final AtomicReference<Disposable> link;

    /**
     * Конструктор.
     * @param bus Канал уведомлений
     * @param audience Подключённые пользователи
     * @param pause Пауза перед первой переподпиской
     */
    public Relay(final Bus bus, final Audience audience, final Duration pause) {
        this(bus, audience, pause, new AtomicReference<>(Disposables.disposed()));
    }

    /**
     * Конструктор.
     * @param bus Канал уведомлений
     * @param audience Подключённые пользователи
     * @param pause Пауза перед первой переподпиской
     * @param link Текущая подписка на канал
     */
    Relay(
        final Bus bus,
        final Audience audience,
        final Duration pause,
        final AtomicReference<Disposable> link
    ) {
        this.bus = bus;
        this.audience = audience;
        this.pause = pause;
        this.link = link;
    }

    @Override
    public void start() {
        this.link.getAndSet(
            this.bus.notices()
                .concatMap(
                    notice -> Mono.fromRunnable(() -> notice.deliver(this.audience))
                        .doOnError(
                            ex -> Relay.LOG.warn(
                                String.format(
                                    "Уведомление %s пропущено: его не удалось доставить",
                                    notice.json()
                                ),
                                ex
                            )
                        )
                        .onErrorComplete()
                )
                .retryWhen(
                    Retry.backoff(Long.MAX_VALUE, this.pause)
                        .maxBackoff(this.pause.multipliedBy(30L))
                        .doBeforeRetry(
                            signal -> Relay.LOG.warn(
                                String.format(
                                    "Подписка на канал потеряна, попытка восстановления номер %d",
                                    signal.totalRetries() + 1L
                                ),
                                signal.failure()
                            )
                        )
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

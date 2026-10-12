/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m30;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.grpc.server.autoconfigure.GrpcServerExecutorProvider;

/**
 * Пул, в котором сервер gRPC выполняет вызовы.
 *
 * <p>По умолчанию grpc-java выполняет вызовы в пуле без предела потоков.
 * Ограниченный пул под нагрузкой отвергает лишние вызовы: клиент получает
 * ошибку сразу, а сервер не плодит потоки. Пул закрывается вместе
 * с контекстом, уже после остановки сервера.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class GrpcExecutor implements GrpcServerExecutorProvider, AutoCloseable {

    /**
     * Пул обработки вызовов.
     */
    private final ExecutorService executor;

    @Override
    public Executor getExecutor() {
        return this.executor;
    }

    @Override
    public void close() {
        this.executor.shutdown();
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Получатель ответа gRPC, запоминающий первый ответ или ошибку.
 * @param <T> Тип ответа
 * @since 1.0
 */
final class FakeObserver<T> implements StreamObserver<T> {

    /**
     * Ответ или ошибка.
     */
    private final CompletableFuture<T> future;

    /**
     * Конструктор пустого получателя.
     */
    FakeObserver() {
        this(new CompletableFuture<>());
    }

    /**
     * Конструктор.
     * @param future Ответ или ошибка
     */
    FakeObserver(final CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void onNext(final T value) {
        this.future.complete(value);
    }

    @Override
    public void onError(final Throwable error) {
        this.future.completeExceptionally(error);
    }

    @Override
    public void onCompleted() {
        this.future.complete(null);
    }

    /**
     * Ответ, дождавшись его не дольше пяти секунд.
     * @return Ответ
     * @throws Exception Если пришла ошибка или ответа нет
     */
    T value() throws Exception {
        return this.future.get(5L, TimeUnit.SECONDS);
    }

    /**
     * Код ошибки, дождавшись её не дольше пяти секунд.
     * @return Код ошибки или OK, если ошибки не было
     * @throws Exception Если ответа нет
     */
    Status.Code code() throws Exception {
        Status.Code code;
        try {
            this.future.get(5L, TimeUnit.SECONDS);
            code = Status.Code.OK;
        } catch (final ExecutionException ex) {
            code = Status.fromThrowable(ex.getCause()).getCode();
        }
        return code;
    }
}

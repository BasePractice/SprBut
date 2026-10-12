/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import ru.sprbut.m30.grpc.Accepted;
import ru.sprbut.m30.grpc.BroadcastMessage;
import ru.sprbut.m30.grpc.NotifierGrpc;
import ru.sprbut.m30.grpc.UserMessage;

/**
 * gRPC-доступ к отправке уведомлений: вызванный экземпляр публикует
 * уведомление в канал Redis, а доставляют его те экземпляры, что подписаны
 * на этот канал.
 * @since 1.0
 */
@GrpcService
public final class GrpcEndpoint extends NotifierGrpc.NotifierImplBase {

    /**
     * Каналы уведомлений.
     */
    private final Bus bus;

    /**
     * Конструктор.
     * @param bus Каналы уведомлений
     */
    public GrpcEndpoint(final Bus bus) {
        this.bus = bus;
    }

    @Override
    public void sendToUser(final UserMessage request, final StreamObserver<Accepted> observer) {
        GrpcEndpoint.reply(
            GrpcEndpoint.notice(request.getEvent(), "user-event", request.getText()).flatMap(
                notice -> GrpcEndpoint.sent(this.bus.publish(request.getUserId(), notice))
            ),
            observer
        );
    }

    @Override
    public void broadcast(
        final BroadcastMessage request,
        final StreamObserver<Accepted> observer
    ) {
        GrpcEndpoint.reply(
            GrpcEndpoint.notice(request.getEvent(), "broadcast", request.getText()).flatMap(
                notice -> GrpcEndpoint.sent(this.bus.publish(notice))
            ),
            observer
        );
    }

    private static Mono<Notice> notice(
        final String event,
        final String fallback,
        final String text
    ) {
        final Mono<Notice> notice;
        if (text.isEmpty()) {
            notice = Mono.error(
                Status.INVALID_ARGUMENT.withDescription("Текст уведомления пуст")
                    .asRuntimeException()
            );
        } else if (event.chars().anyMatch(chr -> chr == '\n' || chr == '\r')) {
            notice = Mono.error(
                Status.INVALID_ARGUMENT.withDescription(
                    String.format("Имя события '%s' содержит перевод строки", event)
                ).asRuntimeException()
            );
        } else if (event.isEmpty()) {
            notice = Mono.just(new TextNotice(fallback, text));
        } else {
            notice = Mono.just(new TextNotice(event, text));
        }
        return notice;
    }

    private static Mono<Long> sent(final Mono<Long> publish) {
        return publish.onErrorMap(
            ex -> Status.UNAVAILABLE.withDescription(
                String.format("Redis не принял уведомление: %s", ex.getMessage())
            ).withCause(ex).asRuntimeException()
        );
    }

    private static void reply(final Mono<Long> sent, final StreamObserver<Accepted> observer) {
        sent.map(count -> Accepted.newBuilder().setReceivers(count).build()).subscribe(
            accepted -> {
                observer.onNext(accepted);
                observer.onCompleted();
            },
            observer::onError
        );
    }
}

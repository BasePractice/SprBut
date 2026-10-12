/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import io.grpc.Status;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import ru.sprbut.m30.grpc.Accepted;
import ru.sprbut.m30.grpc.BroadcastMessage;
import ru.sprbut.m30.grpc.UserMessage;

/**
 * Тесты {@link GrpcEndpoint}.
 * @since 1.0
 */
final class GrpcEndpointTest {

    @Test
    @DisplayName("личное сообщение публикуется в канал пользователя")
    void publishesUserMessageToUserChannel() throws Exception {
        final FakeBus bus = new FakeBus();
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        final String text = UUID.randomUUID().toString();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).sendToUser(
            UserMessage.newBuilder().setUserId(user).setText(text).build(), observer
        );
        observer.value();
        MatcherAssert.assertThat(
            "личное сообщение не попало в канал пользователя",
            bus.user(user).map(ServerSentEvent::data).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("личное сообщение без имени события приходит как user-event")
    void namesUserMessageByDefault() throws Exception {
        final FakeBus bus = new FakeBus();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).sendToUser(
            UserMessage.newBuilder().setUserId(3L).setText("текст").build(), observer
        );
        observer.value();
        MatcherAssert.assertThat(
            "личное сообщение без имени события пришло не как user-event",
            bus.user(3L).map(ServerSentEvent::event).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo("user-event")
        );
    }

    @Test
    @DisplayName("личное сообщение сохраняет заданное имя события")
    void keepsGivenEventOfUserMessage() throws Exception {
        final FakeBus bus = new FakeBus();
        final String event = UUID.randomUUID().toString();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).sendToUser(
            UserMessage.newBuilder().setUserId(4L).setEvent(event).setText("текст").build(),
            observer
        );
        observer.value();
        MatcherAssert.assertThat(
            "личное сообщение потеряло заданное имя события",
            bus.user(4L).map(ServerSentEvent::event).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo(event)
        );
    }

    @Test
    @DisplayName("общее сообщение публикуется в общий канал")
    void publishesBroadcastMessageToBroadcastChannel() throws Exception {
        final FakeBus bus = new FakeBus();
        final String text = UUID.randomUUID().toString();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).broadcast(
            BroadcastMessage.newBuilder().setText(text).build(), observer
        );
        observer.value();
        MatcherAssert.assertThat(
            "общее сообщение не попало в общий канал",
            bus.broadcasts().map(ServerSentEvent::data).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("общее сообщение без имени события приходит как broadcast")
    void namesBroadcastMessageByDefault() throws Exception {
        final FakeBus bus = new FakeBus();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).broadcast(
            BroadcastMessage.newBuilder().setText("текст").build(), observer
        );
        observer.value();
        MatcherAssert.assertThat(
            "общее сообщение без имени события пришло не как broadcast",
            bus.broadcasts().map(ServerSentEvent::event).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo("broadcast")
        );
    }

    @Test
    @DisplayName("ответ называет число подписчиков личного канала")
    void answersWithReceiversOfUserChannel() throws Exception {
        final FakeBus bus = new FakeBus();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        bus.user(5L).subscribe();
        bus.user(5L).subscribe();
        new GrpcEndpoint(bus).sendToUser(
            UserMessage.newBuilder().setUserId(5L).setText("текст").build(), observer
        );
        MatcherAssert.assertThat(
            "ответ ошибся в числе подписчиков личного канала",
            observer.value().getReceivers(),
            Matchers.equalTo(2L)
        );
    }

    @Test
    @DisplayName("пустой текст отвергается кодом INVALID_ARGUMENT")
    void rejectsEmptyText() throws Exception {
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(new FakeBus()).broadcast(BroadcastMessage.newBuilder().build(), observer);
        MatcherAssert.assertThat(
            "пустой текст не отвергнут кодом INVALID_ARGUMENT",
            observer.code(),
            Matchers.equalTo(Status.Code.INVALID_ARGUMENT)
        );
    }

    @Test
    @DisplayName("имя события с переводом строки отвергается кодом INVALID_ARGUMENT")
    void rejectsEventWithLineBreak() throws Exception {
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(new FakeBus()).sendToUser(
            UserMessage.newBuilder().setUserId(6L).setEvent(String.format("user%ndata: подлог"))
                .setText("текст").build(),
            observer
        );
        MatcherAssert.assertThat(
            "имя события с переводом строки не отвергнуто кодом INVALID_ARGUMENT",
            observer.code(),
            Matchers.equalTo(Status.Code.INVALID_ARGUMENT)
        );
    }

    @Test
    @DisplayName("отвергнутое сообщение не публикуется")
    void doesntPublishRejectedMessage() throws Exception {
        final FakeBus bus = new FakeBus();
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(bus).sendToUser(
            UserMessage.newBuilder().setUserId(7L).build(), observer
        );
        observer.code();
        MatcherAssert.assertThat(
            "отвергнутое сообщение попало в канал пользователя",
            bus.user(7L).take(Duration.ofMillis(100L)).collectList()
                .block(Duration.ofSeconds(5L)),
            Matchers.empty()
        );
    }

    @Test
    @DisplayName("недоступный Redis даёт код UNAVAILABLE")
    void reportsUnavailableRedis() throws Exception {
        final FakeObserver<Accepted> observer = new FakeObserver<>();
        new GrpcEndpoint(new BrokenBus()).broadcast(
            BroadcastMessage.newBuilder().setText("текст").build(), observer
        );
        MatcherAssert.assertThat(
            "недоступный Redis не дал кода UNAVAILABLE",
            observer.code(),
            Matchers.equalTo(Status.Code.UNAVAILABLE)
        );
    }
}

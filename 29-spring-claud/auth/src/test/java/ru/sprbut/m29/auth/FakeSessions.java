/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сессии в памяти для тестов с той же ротацией, что и в базе:
 * повтор обменянного токена закрывает сессию.
 * @since 1.0
 */
final class FakeSessions implements Sessions {

    /**
     * Выданные refresh-токены и их сессии.
     */
    private final Map<String, Ticket> issued;

    /**
     * Обменянные refresh-токены.
     */
    private final Set<String> used;

    /**
     * Закрытые сессии.
     */
    private final Set<UUID> closed;

    /**
     * Конструктор пустых сессий.
     */
    FakeSessions() {
        this(
            new ConcurrentHashMap<>(0),
            ConcurrentHashMap.newKeySet(),
            ConcurrentHashMap.newKeySet()
        );
    }

    /**
     * Конструктор.
     * @param issued Выданные refresh-токены и их сессии
     * @param used   Обменянные refresh-токены
     * @param closed Закрытые сессии
     */
    FakeSessions(
        final Map<String, Ticket> issued,
        final Set<String> used,
        final Set<UUID> closed
    ) {
        this.issued = issued;
        this.used = used;
        this.closed = closed;
    }

    @Override
    public Ticket open(final UUID account) {
        final Ticket ticket = new Ticket(account, UUID.randomUUID(), UUID.randomUUID().toString());
        this.issued.put(ticket.refresh(), ticket);
        return ticket;
    }

    @Override
    public Optional<Ticket> rotate(final String refresh) {
        final Optional<Ticket> found = Optional.ofNullable(this.issued.get(refresh));
        found.filter(ticket -> this.used.contains(refresh)).ifPresent(
            ticket -> this.closed.add(ticket.session())
        );
        return found.filter(ticket -> this.used.add(refresh))
            .filter(ticket -> !this.closed.contains(ticket.session()))
            .map(
                ticket -> {
                    final Ticket next = new Ticket(
                        ticket.account(), ticket.session(), UUID.randomUUID().toString()
                    );
                    this.issued.put(next.refresh(), next);
                    return next;
                }
            );
    }

    @Override
    public boolean active(final UUID session) {
        return this.issued.values().stream().anyMatch(ticket -> ticket.session().equals(session))
            && !this.closed.contains(session);
    }

    @Override
    public void close(final UUID account, final String refresh) {
        Optional.ofNullable(this.issued.get(refresh))
            .filter(ticket -> ticket.account().equals(account))
            .ifPresent(ticket -> this.closed.add(ticket.session()));
    }

    @Override
    public void revoke(final UUID account) {
        this.issued.values().stream()
            .filter(ticket -> ticket.account().equals(account))
            .forEach(ticket -> this.closed.add(ticket.session()));
    }

    @Override
    public List<Map<String, Object>> list(final UUID account) {
        return this.issued.values().stream()
            .filter(ticket -> ticket.account().equals(account))
            .map(Ticket::session)
            .distinct()
            .filter(this::active)
            .<Map<String, Object>>map(session -> Map.of("id", session))
            .toList();
    }
}

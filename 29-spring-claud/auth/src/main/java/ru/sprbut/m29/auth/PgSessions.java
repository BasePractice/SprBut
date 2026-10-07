/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Сессии в таблицах PostgreSQL; refresh-токены хранятся только хешами SHA-256.
 * @since 1.0
 */
public final class PgSessions implements Sessions {

    /**
     * Источник случайных токенов.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Клиент базы.
     */
    private final JdbcClient jdbc;

    /**
     * Транзакции.
     */
    private final TransactionOperations txn;

    /**
     * Часы.
     */
    private final Clock clock;

    /**
     * Срок жизни refresh-токена.
     */
    private final Duration ttl;

    /**
     * Конструктор.
     * @param jdbc  Клиент базы
     * @param txn   Транзакции
     * @param clock Часы
     * @param ttl   Срок жизни refresh-токена
     */
    public PgSessions(
        final JdbcClient jdbc,
        final TransactionOperations txn,
        final Clock clock,
        final Duration ttl
    ) {
        this.jdbc = jdbc;
        this.txn = txn;
        this.clock = clock;
        this.ttl = ttl;
    }

    @Override
    public Ticket open(final UUID account) {
        final UUID session = UUID.randomUUID();
        final OffsetDateTime now = this.now();
        return this.txn.execute(
            status -> {
                this.jdbc.sql(
                    String.join(
                        " ",
                        "INSERT INTO session (id, account, created, expires, revoked)",
                        "VALUES (:id, :account, :created, :expires, false)"
                    )
                ).params(
                    Map.of(
                        "id", session,
                        "account", account,
                        "created", now,
                        "expires", now.plus(this.ttl)
                    )
                ).update();
                return new Ticket(account, session, this.issue(session, now));
            }
        );
    }

    @Override
    public Optional<Ticket> rotate(final String refresh) {
        final OffsetDateTime now = this.now();
        return this.txn.execute(
            status -> this.jdbc.sql(
                String.join(
                    " ",
                    "SELECT r.session, r.used, r.expires, s.account, s.revoked",
                    "FROM refresh r JOIN session s ON s.id = r.session",
                    "WHERE r.hash = :hash FOR UPDATE"
                )
            ).param("hash", PgSessions.hash(refresh)).query(
                (row, num) -> new Stored(
                    row.getObject("account", UUID.class),
                    row.getObject("session", UUID.class),
                    row.getBoolean("used"),
                    row.getBoolean("revoked") || now.isAfter(
                        row.getObject("expires", OffsetDateTime.class)
                    )
                )
            ).optional().flatMap(stored -> this.rotated(stored, refresh, now))
        );
    }

    @Override
    public boolean active(final UUID session) {
        return this.jdbc.sql(
            "SELECT count(*) FROM session WHERE id = :id AND NOT revoked AND expires > :now"
        ).params(Map.of("id", session, "now", this.now())).query(Long.class).single() > 0;
    }

    @Override
    public void close(final UUID account, final String refresh) {
        this.jdbc.sql(
            String.join(
                " ",
                "UPDATE session SET revoked = true WHERE account = :account",
                "AND id = (SELECT session FROM refresh WHERE hash = :hash)"
            )
        ).params(Map.of("account", account, "hash", PgSessions.hash(refresh))).update();
    }

    @Override
    public void revoke(final UUID account) {
        this.jdbc.sql("UPDATE session SET revoked = true WHERE account = :account")
            .param("account", account)
            .update();
    }

    @Override
    public List<Map<String, Object>> list(final UUID account) {
        return this.jdbc.sql(
            String.join(
                " ",
                "SELECT id, created, expires FROM session",
                "WHERE account = :account AND NOT revoked AND expires > :now ORDER BY created"
            )
        ).params(Map.of("account", account, "now", this.now())).query(
            (row, num) -> {
                final Map<String, Object> json = new LinkedHashMap<>(3);
                json.put("id", row.getObject("id", UUID.class));
                json.put("created", row.getObject("created", OffsetDateTime.class));
                json.put("expires", row.getObject("expires", OffsetDateTime.class));
                return json;
            }
        ).list();
    }

    private Optional<Ticket> rotated(
        final Stored stored,
        final String refresh,
        final OffsetDateTime now
    ) {
        final Optional<Ticket> ticket;
        if (stored.used()) {
            this.jdbc.sql("UPDATE session SET revoked = true WHERE id = :id")
                .param("id", stored.session())
                .update();
            ticket = Optional.empty();
        } else if (stored.dead()) {
            ticket = Optional.empty();
        } else {
            this.jdbc.sql("UPDATE refresh SET used = true WHERE hash = :hash")
                .param("hash", PgSessions.hash(refresh))
                .update();
            this.jdbc.sql("UPDATE session SET expires = :expires WHERE id = :id")
                .params(Map.of("expires", now.plus(this.ttl), "id", stored.session()))
                .update();
            ticket = Optional.of(
                new Ticket(stored.account(), stored.session(), this.issue(stored.session(), now))
            );
        }
        return ticket;
    }

    private String issue(final UUID session, final OffsetDateTime now) {
        final byte[] bytes = new byte[32];
        PgSessions.RANDOM.nextBytes(bytes);
        final String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        this.jdbc.sql(
            String.join(
                " ",
                "INSERT INTO refresh (hash, session, used, expires)",
                "VALUES (:hash, :session, false, :expires)"
            )
        ).params(
            Map.of(
                "hash", PgSessions.hash(refresh),
                "session", session,
                "expires", now.plus(this.ttl)
            )
        ).update();
        return refresh;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(this.clock.instant(), ZoneOffset.UTC);
    }

    private static String hash(final String refresh) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(
                    refresh.getBytes(StandardCharsets.UTF_8)
                )
            );
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Алгоритм SHA-256 недоступен в этой JVM", ex);
        }
    }

    /**
     * Найденный refresh-токен и состояние его сессии.
     * @param account Идентификатор учётки
     * @param session Идентификатор сессии
     * @param used    Был ли токен уже обменян
     * @param dead    Закрыта или истекла ли сессия либо токен
     * @since 1.0
     */
    private record Stored(UUID account, UUID session, boolean used, boolean dead) {
    }
}

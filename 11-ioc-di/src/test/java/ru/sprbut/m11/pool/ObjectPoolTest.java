/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m11.pool;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Пул объектов: управление временем жизни вместо new.
 * @since 1.0
 */
@DisplayName("Пул объектов: управление временем жизни вместо new")
final class ObjectPoolTest {

    @Test
    @DisplayName("Пустой пул просит объект у фабрики")
    void createsObjectWhenNothingToReuse() {
        MatcherAssert.assertThat(
            "empty pool cannot ask the factory for a new object",
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 2).object().toString(),
            Matchers.equalTo("свежий")
        );
    }

    @Test
    @DisplayName("Отданный пулу объект достаётся следующему владельцу")
    void reusesReleasedObject() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 2);
        final StringBuilder first = pool.object();
        pool.release(first);
        MatcherAssert.assertThat(
            "released object cannot come back from the pool",
            pool.object(),
            Matchers.sameInstance(first)
        );
    }

    @Test
    @DisplayName("Переиспользованный объект приходит очищенным")
    void cleansReusedObject() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 2);
        pool.release(pool.object().append(" и уже поработавший"));
        MatcherAssert.assertThat(
            "reused object cannot keep the traces of its previous owner",
            pool.object().toString(),
            Matchers.emptyString()
        );
    }

    @Test
    @DisplayName("Только что созданный объект пул не чистит")
    void keepsFreshObjectUntouched() {
        final ObjectPoolTest.Fake factory = new ObjectPoolTest.Fake();
        new ObjectPool.Default<>(factory, 2).object();
        MatcherAssert.assertThat(
            "fresh object cannot be cleaned before anyone used it",
            factory.cleans(),
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("Сверх ёмкости пул объекты не хранит")
    void dropsWhatDoesNotFit() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 1);
        final StringBuilder first = pool.object();
        final StringBuilder second = pool.object();
        pool.release(first);
        pool.release(second);
        pool.object();
        MatcherAssert.assertThat(
            "pool cannot drop the object that does not fit its capacity",
            pool.object(),
            Matchers.not(Matchers.sameInstance(second))
        );
    }

    @Test
    @DisplayName("Аренда отдаёт объект пула, а не копию")
    void leasesObjectOfThePool() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 2);
        final StringBuilder taken = pool.object();
        pool.release(taken);
        try (ObjectPool.Lease<StringBuilder> lease = pool.lease()) {
            MatcherAssert.assertThat(
                "lease cannot hand out the object waiting in the pool",
                lease.object(),
                Matchers.sameInstance(taken)
            );
        }
    }

    @Test
    @DisplayName("На выходе из try арендованный объект возвращается сам")
    void returnsLeasedObjectOnClose() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 2);
        final StringBuilder leased;
        try (ObjectPool.Lease<StringBuilder> lease = pool.lease()) {
            leased = lease.object();
        }
        MatcherAssert.assertThat(
            "closed lease cannot put the object back into the pool",
            pool.object(),
            Matchers.sameInstance(leased)
        );
    }

    @Test
    @DisplayName("Вместо объекта пулу нельзя отдать null")
    void rejectsNull() {
        final ObjectPool<StringBuilder> pool =
            new ObjectPool.Default<>(new ObjectPoolTest.Fake(), 1);
        MatcherAssert.assertThat(
            "pool cannot refuse null with a readable message",
            Assertions.assertThrows(
                NullPointerException.class, () -> pool.release(null)
            ).getMessage(),
            Matchers.containsString("null")
        );
    }

    /**
     * Фабрика-фейк: делает узнаваемый объект и считает очистки, чтобы работу
     * пула было видно снаружи.
     * @since 1.0
     */
    private static final class Fake implements ObjectPool.Factory<StringBuilder> {

        /**
         * Сколько раз пул просил очистить объект.
         */
        private int cleans;

        @Override
        public StringBuilder instance() {
            return new StringBuilder("свежий");
        }

        @Override
        public void clean(final StringBuilder object) {
            this.cleans += 1;
            object.setLength(0);
        }

        int cleans() {
            return this.cleans;
        }
    }
}

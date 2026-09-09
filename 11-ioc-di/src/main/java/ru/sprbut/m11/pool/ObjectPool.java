/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.pool;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Пул объектов: та же инверсия управления, что и в контейнере, но не над
 * зависимостями, а над <b>временем жизни</b>.
 *
 * <p>Фабрика шага 2 умеет одно — создавать. Пул добавляет к ней вторую половину
 * работы контейнера: он решает, создать объект заново или отдать уже созданный.
 * Клиент не пишет {@code new} и не решает, когда объекту умереть; он просит
 * объект и возвращает его обратно. Именно так устроены пулы соединений,
 * которыми Spring Boot управляет через {@code DataSource}.</p>
 *
 * <p>Пул изменяем, и это расхождение с RULE.md намеренное: хранимое между
 * вызовами состояние здесь и есть предмет разговора — без него нечего
 * переиспользовать.</p>
 *
 * @param <T> Тип объектов пула
 * @since 1.0
 */
public interface ObjectPool<T> {

    /**
     * Объект для работы: отложенный в пул, если такой есть, иначе новый.
     * @return Объект для работы
     */
    T object();

    /**
     * Отдаёт объект пулу: с этого момента он больше не принадлежит вызывающему.
     * @param object Объект, который больше не нужен
     */
    void release(T object);

    /**
     * Тот же объект, но взятый в аренду: вернуть его пулу — забота
     * {@code try}-с-ресурсами, а не вызывающего.
     * @return Аренда объекта
     */
    Lease<T> lease();

    /**
     * Фабрика пула: она знает о самом объекте всё, чего не знает пул.
     *
     * <p>Два метода вместо одного — потому что переиспользование дороже создания
     * ровно на очистку. Объект, у которого осталось состояние прошлого
     * владельца, — самая дорогая ошибка пула, и она вынесена в отдельный
     * метод, чтобы её нельзя было забыть.</p>
     *
     * @param <T> Тип объектов пула
     * @since 1.0
     */
    interface Factory<T> {

        /**
         * Новый объект: пул зовёт фабрику, когда переиспользовать нечего.
         * @return Новый объект
         */
        T instance();

        /**
         * Стирает следы прошлого владельца.
         * @param object Объект, который пул готовит к новой работе
         */
        void clean(T object);
    }

    /**
     * Аренда: объект пула вместе с обещанием вернуть его на место.
     *
     * <p>Ручная пара {@code object}/{@code release} — это тот же
     * {@code malloc}/{@code free}, от которого Java когда-то избавилась:
     * забытый {@code release} превращает пул в бесполезный генератор мусора,
     * а лишний — раздаёт один объект двум владельцам сразу. Аренда закрывает
     * обе дыры, потому что за возврат отвечает язык:</p>
     *
     * <pre>
     * try (ObjectPool.Lease&lt;Buffer&gt; lease = pool.lease()) {
     *     lease.object().write(data);
     * }
     * </pre>
     *
     * <p>{@code close} объявлен заново без {@code throws}: аренда ничем
     * не рискует, и заставлять вызывающего ловить {@code Exception}
     * было бы неправдой.</p>
     *
     * @param <T> Тип объектов пула
     * @since 1.0
     */
    interface Lease<T> extends AutoCloseable {

        /**
         * Арендованный объект.
         * @return Арендованный объект
         */
        T object();

        @Override
        void close();
    }

    /**
     * Аренда, которая знает свой пул.
     *
     * @param <T> Тип объектов пула
     * @param pool Пул, которому вернётся объект
     * @param object Арендованный объект
     * @since 1.0
     */
    record Rented<T>(ObjectPool<T> pool, T object) implements Lease<T> {

        @Override
        public void close() {
            this.pool.release(this.object);
        }
    }

    /**
     * Пул с ограниченной ёмкостью: сверх неё возвращённые объекты не хранятся,
     * а достаются сборщику мусора.
     *
     * <p>Очередь взята потокобезопасная, и это не украшение: пул почти всегда
     * общий для нескольких потоков. Проверять «пусто ли» и брать объект двумя
     * отдельными вызовами нельзя — между ними успевает вклиниться чужой поток,
     * поэтому берётся один атомарный {@code poll}.</p>
     *
     * @param <T> Тип объектов пула
     * @since 1.0
     */
    final class Default<T> implements ObjectPool<T> {

        /**
         * Отложенные объекты, ждущие следующего владельца.
         */
        private final Queue<T> idle;

        /**
         * Фабрика, создающая и очищающая объекты.
         */
        private final Factory<T> factory;

        /**
         * Основной конструктор.
         * @param factory Фабрика, создающая и очищающая объекты
         * @param capacity Сколько объектов пул готов хранить
         */
        public Default(final Factory<T> factory, final int capacity) {
            this.factory = factory;
            this.idle = new ArrayBlockingQueue<>(capacity);
        }

        @Override
        public T object() {
            final T reused = this.idle.poll();
            final T object;
            if (reused == null) {
                object = Objects.requireNonNull(
                    this.factory.instance(), "Фабрика пула вернула null вместо объекта"
                );
            } else {
                this.factory.clean(reused);
                object = reused;
            }
            return object;
        }

        @Override
        public void release(final T object) {
            this.idle.offer(
                Objects.requireNonNull(object, "В пул нельзя вернуть null")
            );
        }

        @Override
        public Lease<T> lease() {
            return new Rented<>(this, this.object());
        }
    }
}

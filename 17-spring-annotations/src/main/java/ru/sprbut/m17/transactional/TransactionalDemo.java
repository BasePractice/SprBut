/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m17.transactional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 147: «{@code @Transactional} — через AOP-прокси».
 *
 * <p>Здесь настоящий {@code @Transactional} со своим менеджером транзакций,
 * который вместо базы пишет в список. Так видно <b>механику</b>, не отвлекаясь
 * на JDBC: аннотация — только метаданные, всю работу делает прокси.</p>
 *
 * <p>Отсюда же все известные особенности:
 * <ul>
 * <li>self-invocation не открывает транзакцию (модуль 15);</li>
 * <li>по умолчанию откат происходит только на unchecked-исключениях;</li>
 * <li>{@code private}-метод аннотацией не перехватывается — прокси видит
 * только публичный API.</li>
 * </ul></p>
 *
 * @since 1.0
 */
public final class TransactionalDemo {
    /**
     * Журнал операций менеджера транзакций.
     */
    public static final List<String> LOG = new ArrayList<>();

    private TransactionalDemo() {
    }

    /**
     * Сброс состояния.
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public static void reset() {
        LOG.clear();
    }

    /**
     * Менеджер транзакций, который вместо БД пишет в журнал.
     * @since 1.0
     */
    public static class LoggingTransactionManager implements PlatformTransactionManager {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public LoggingTransactionManager() {
            // нечего инициализировать
        }

        @Override
        public TransactionStatus getTransaction(final TransactionDefinition definition)
                throws TransactionException {
            LOG.add("begin");
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(final TransactionStatus status) throws TransactionException {
            LOG.add("commit");
        }

        @Override
        public void rollback(final TransactionStatus status) throws TransactionException {
            LOG.add("rollback");
        }
    }

    /**
     * Порядок.
     * @since 1.0
     */
    @Service
    public static class OrderService {
        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public OrderService() {
            // нечего инициализировать
        }

        /**
         * Сохранение.
         * @param order Порядок
         * @return Сохранение
         */
        @Transactional
        public String save(final String order) {
            LOG.add("save:" + order);
            return order;
        }

        /**
         * Unchecked-исключение — откат по умолчанию.
         */
        @Transactional
        public void failUnchecked() {
            LOG.add("work");
            throw new IllegalStateException("что-то пошло не так");
        }

        /**
         * Checked-исключение — по умолчанию транзакция <b>коммитится</b>.
         */
        @Transactional
        public void failChecked() throws Exception {
            LOG.add("work");
            throw new Exception("проверяемое исключение");
        }

        /**
         * Метод без аннотации — транзакции не будет.
         * @param order Порядок
         * @return Метод без аннотации — транзакции не будет
         */
        public String saveWithoutTransaction(final String order) {
            LOG.add("save:" + order);
            return order;
        }

        /**
         * Self-invocation: транзакция не откроется, прокси в стороне.
         * @param order Порядок
         * @return Self-invocation: транзакция не откроется, прокси в стороне
         */
        public String saveViaThis(final String order) {
            return this.save(order);
        }

        /**
         * Явное указание откатываться и на checked-исключениях.
         */
        @Transactional(rollbackFor = Exception.class)
        public void failCheckedWithRollback() throws Exception {
            LOG.add("work");
            throw new Exception("проверяемое исключение");
        }
    }

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    @EnableTransactionManagement
    public static class Config {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Config() {
            // нечего инициализировать
        }

        /**
         * Менеджер транзакций.
         * @return Менеджер транзакций
         */
        @Bean
        public PlatformTransactionManager transactionManager() {
            return new LoggingTransactionManager();
        }

        /**
         * Порядок.
         * @return Порядок
         */
        @Bean
        public OrderService orderService() {
            return new OrderService();
        }
    }
}

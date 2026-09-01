/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
// тема раздела — @Transactional поверх собственного менеджера транзакций:
// менеджер, сервис и конфигурация показаны вместе
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle QualifyInnerClassCheck disable
package ru.sprbut.m17.transactional;

import java.util.ArrayList;
import java.util.List;
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
    public static final List<String> LOG = new ArrayList<>(0);

    private TransactionalDemo() {
    }

    /**
     * Сброс состояния.
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static void reset() {
        TransactionalDemo.LOG.clear();
    }

    /**
     * Менеджер транзакций, который вместо БД пишет в журнал.
     * @since 1.0
     */
    public static final class LoggingTransactionManager implements PlatformTransactionManager {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public LoggingTransactionManager() {
            // нечего инициализировать
        }

        @Override
        public TransactionStatus getTransaction(final TransactionDefinition definition)
            throws TransactionException {
            TransactionalDemo.LOG.add("begin");
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(final TransactionStatus status) throws TransactionException {
            TransactionalDemo.LOG.add("commit");
        }

        @Override
        public void rollback(final TransactionStatus status) throws TransactionException {
            TransactionalDemo.LOG.add("rollback");
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
            TransactionalDemo.LOG.add(String.format("save:%s", order));
            return order;
        }

        /**
         * Unchecked-исключение — откат по умолчанию.
         */
        @Transactional
        public void failUnchecked() {
            TransactionalDemo.LOG.add("work");
            throw new IllegalStateException("что-то пошло не так");
        }

        /**
         * Checked-исключение — по умолчанию транзакция <b>коммитится</b>.
         * Тип исключения здесь и есть предмет примера: важно именно то,
         * что оно проверяемое, а не какое оно по смыслу.
         * @throws Exception Всегда
         */
        @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
        @Transactional
        public void failChecked() throws Exception {
            TransactionalDemo.LOG.add("work");
            throw new Exception("проверяемое исключение");
        }

        /**
         * Метод без аннотации — транзакции не будет.
         * @param order Порядок
         * @return Метод без аннотации — транзакции не будет
         */
        public String saveWithoutTransaction(final String order) {
            TransactionalDemo.LOG.add(String.format("save:%s", order));
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
         * @throws Exception Всегда
         */
        @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
        @Transactional(rollbackFor = Exception.class)
        public void failCheckedWithRollback() throws Exception {
            TransactionalDemo.LOG.add("work");
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

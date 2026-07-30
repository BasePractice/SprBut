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
 * <p>
 * Здесь настоящий {@code @Transactional} со своим менеджером транзакций,
 * который вместо базы пишет в список. Так видно <b>механику</b>, не отвлекаясь
 * на JDBC: аннотация — только метаданные, всю работу делает прокси.
 * <p>
 * Отсюда же все известные особенности:
 * <ul>
 *   <li>self-invocation не открывает транзакцию (модуль 15);</li>
 *   <li>по умолчанию откат происходит только на unchecked-исключениях;</li>
 *   <li>{@code private}-метод аннотацией не перехватывается — прокси видит
 *       только публичный API.</li>
 * </ul>
 */
public final class TransactionalDemo {

    private TransactionalDemo() {
    }

    /** Журнал операций менеджера транзакций. */
    public static final List<String> LOG = new ArrayList<>();

    public static void reset() {
        LOG.clear();
    }

    /** Менеджер транзакций, который вместо БД пишет в журнал. */
    public static class LoggingTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition)
                throws TransactionException {
            LOG.add("begin");
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {
            LOG.add("commit");
        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
            LOG.add("rollback");
        }
    }

    @Service
    public static class OrderService {

        @Transactional
        public String save(String order) {
            LOG.add("save:" + order);
            return order;
        }

        /** Unchecked-исключение — откат по умолчанию. */
        @Transactional
        public void failUnchecked() {
            LOG.add("work");
            throw new IllegalStateException("что-то пошло не так");
        }

        /** Checked-исключение — по умолчанию транзакция <b>коммитится</b>. */
        @Transactional
        public void failChecked() throws Exception {
            LOG.add("work");
            throw new Exception("проверяемое исключение");
        }

        /** Явное указание откатываться и на checked-исключениях. */
        @Transactional(rollbackFor = Exception.class)
        public void failCheckedWithRollback() throws Exception {
            LOG.add("work");
            throw new Exception("проверяемое исключение");
        }

        /** Метод без аннотации — транзакции не будет. */
        public String saveWithoutTransaction(String order) {
            LOG.add("save:" + order);
            return order;
        }

        /** Self-invocation: транзакция не откроется, прокси в стороне. */
        public String saveViaThis(String order) {
            return save(order);
        }
    }

    @Configuration
    @EnableTransactionManagement
    public static class Config {

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new LoggingTransactionManager();
        }

        @Bean
        public OrderService orderService() {
            return new OrderService();
        }
    }
}

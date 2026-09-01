/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// beanName — имя параметра из контракта BeanPostProcessor
// @checkstyle ParameterNameCheck disable
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14;

import java.lang.reflect.Proxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * Шаги 4 и 6: {@link BeanPostProcessor} — главная точка расширения контейнера.
 *
 * <p>Через неё Spring реализует почти всё «магическое»: {@code @Autowired}
 * (AutowiredAnnotationBeanPostProcessor), {@code @PostConstruct}
 * (CommonAnnotationBeanPostProcessor), AOP-прокси
 * (AnnotationAwareAspectJAutoProxyCreator).</p>
 *
 * <p>Ключевая деталь: {@code postProcessAfterInitialization} может вернуть
 * <b>другой объект</b>. Именно так на месте бина оказывается прокси —
 * и именно поэтому в контексте лежит не тот экземпляр, который создал
 * ваш конструктор (модуль 15).</p>
 *
 * @since 1.0
 */
public final class AuditBeanPostProcessor implements BeanPostProcessor {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public AuditBeanPostProcessor() {
        // нечего инициализировать
    }

    @Override
    public Object postProcessBeforeInitialization(final @NonNull Object bean,
        final @NonNull String beanName) throws BeansException {
        if (bean instanceof ManagedBean || bean instanceof AuditBeanPostProcessor.AuditableBean) {
            LifecycleLog.record(String.format("4-bpp-before:%s", beanName));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final @NonNull Object bean,
        final @NonNull String beanName) throws BeansException {
        final Object exposed;
        if (bean instanceof ManagedBean) {
            LifecycleLog.record(String.format("6-bpp-after:%s", beanName));
            exposed = bean;
        } else if (bean instanceof Auditable auditable) {
            LifecycleLog.record(String.format("6-bpp-after:%s", beanName));
            exposed = AuditBeanPostProcessor.wrapped(auditable);
        } else {
            exposed = bean;
        }
        return exposed;
    }

    private static Object wrapped(final Auditable auditable) {
        return Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[]{Auditable.class},
            (proxy, method, args) -> {
                final Object result = method.invoke(auditable, args);
                final Object shown;
                if ("describe".equals(method.getName())) {
                    shown = String.format("%s (через прокси)", result);
                } else {
                    shown = result;
                }
                return shown;
            }
        );
    }

    /**
     * Интерфейс, реализации которого будут подменены прокси.
     * @since 1.0
     */
    @SuppressWarnings("PMD.ImplicitFunctionalInterface")
    public interface Auditable {

        /**
         * Описание.
         * @return Описание
         */
        String describe();
    }

    /**
     * Бин, который на выходе из контейнера окажется прокси, а не собой.
     * @since 1.0
     */
    public static final class AuditableBean implements Auditable {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public AuditableBean() {
            // нечего инициализировать
        }

        @Override
        public String describe() {
            return "оригинал";
        }
    }
}

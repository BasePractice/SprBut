/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.extended;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import ru.sprbut.m23.audit.Audited;

/**
 * <b>Расширенный итог курса.</b>
 *
 * <p>Приложение, которое рассказывает о себе само: карта живого контейнера,
 * построенная рефлексией по нему же.</p>
 *
 * <p>В одном классе сходится всё, чему был посвящён курс. Рефлексия читает
 * настоящие классы бинов и ищет в них аннотации (модули 01–06). Аннотация
 * {@link Audited} оказывается всего лишь меткой, которую кто-то должен прочесть
 * (модуль 05). Контейнер отдаёт определения бинов и их области видимости
 * (модули 11–14). {@code AopProxyUtils} снимает обёртку и показывает, что бин
 * в контексте — не тот объект, который написан в исходниках (модуль 15).</p>
 *
 * <p>Практический смысл тот же, что у {@code /actuator/beans}: когда поведение
 * приложения расходится с кодом, разница почти всегда объясняется одной
 * из строчек этой карты.</p>
 *
 * @since 1.0
 */
@Component
public final class ContextMap {

    /**
     * Контекст.
     */
    private final ConfigurableApplicationContext context;

    /**
     * Основной конструктор.
     * @param context Контекст
     */
    public ContextMap(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Карточки прикладных бинов — только своих, без инфраструктуры Spring.
     * @return Карточки прикладных бинов — только своих, без инфраструктуры Spring
     */
    public List<BeanCard> cards() {
        final ConfigurableListableBeanFactory beans = this.context.getBeanFactory();
        return Arrays.stream(beans.getBeanDefinitionNames())
            .filter(name -> this.mine(beans.getBeanDefinition(name).getBeanClassName()))
            .map(name -> this.card(name, beans))
            .toList();
    }

    /**
     * Проксирован ли бин — то есть перехватываются ли вызовы его методов.
     * @param name Имя
     * @return Проксирован ли бин — то есть перехватываются ли вызовы его методов
     */
    public boolean proxied(final String name) {
        return AopUtils.isAopProxy(this.context.getBean(name));
    }

    /**
     * Вид прокси: JDK-прокси вокруг интерфейса или CGLIB-подкласс.
     * @param name Имя
     * @return Вид прокси: JDK-прокси вокруг интерфейса или CGLIB-подкласс
     */
    public String proxy(final String name) {
        final Object bean = this.context.getBean(name);
        if (AopUtils.isJdkDynamicProxy(bean)) {
            return "jdk";
        }
        if (AopUtils.isCglibProxy(bean)) {
            return "cglib";
        }
        return "none";
    }

    private BeanCard card(final String name, final ConfigurableListableBeanFactory beans) {
        final Class<?> type = AopProxyUtils.ultimateTargetClass(this.context.getBean(name));
        return new BeanCard(
            name,
            type.getName(),
            beans.getBeanDefinition(name).getScope().isEmpty()
                ? "singleton"
                : beans.getBeanDefinition(name).getScope(),
            this.audited(type)
        );
    }

    private List<String> audited(final Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Audited.class))
            .map(this::operation)
            .sorted()
            .toList();
    }

    // @checkstyle NonStaticMethodCheck (3 lines)
    private String operation(final Method method) {
        final Audited audited = method.getAnnotation(Audited.class);
        return audited.value().isBlank() ? method.getName() : audited.value();
    }

    private static boolean mine(final String type) {
        return type != null && type.startsWith("ru.sprbut.m23");
    }
}

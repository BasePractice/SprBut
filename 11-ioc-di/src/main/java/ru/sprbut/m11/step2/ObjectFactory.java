/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.step2;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import ru.sprbut.m11.domain.SmsSender;

/**
 * Слайд 85: «Фабрика — обеспечивает создание экземпляров объектов.
 * DI использует IoC и фабрики».
 *
 * <p>Ручная сборка, вынесенная в одно место. Это уже прообраз контейнера:
 * фабрика знает, как создать объект, и хранит созданные синглтоны.</p>
 *
 * <p>Хорошо видно, чего такой подход стоит: <b>каждую связь надо описать руками</b>.
 * Добавили зависимость в конструктор — правьте фабрику. На двух десятках
 * сервисов это превращается в отдельный неподъёмный класс — и ровно поэтому
 * появились IoC-контейнеры.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
public class ObjectFactory {

    /**
     * Синглтоны.
     */
    private final Map<String, Object> singletons = new HashMap<>(0);

    /**
     * Канал.
     */
    private final String channel;

    /**
     * Основной конструктор.
     * @param channel Канал
     */
    public ObjectFactory(final String channel) {
        this.channel = channel;
    }

    /**
     * Синглтон: создаём один раз, дальше отдаём тот же экземпляр.
     * @return Отправитель уведомлений
     */
    public NotificationSender notificationSender() {
        return (NotificationSender) this.singleton("sender", this::sender);
    }

    /**
     * Калькулятор цены.
     * @return Калькулятор цены
     */
    public PriceCalculator priceCalculator() {
        return (PriceCalculator) this.singleton(
            "calculator", () -> new PriceCalculator(new BigDecimal("0.20"))
        );
    }

    /**
     * Сборка графа: сначала зависимости, потом сам объект.
     * @return Сервис заказов со всеми зависимостями
     */
    public ManualOrderService orderService() {
        return (ManualOrderService) this.singleton(
            "orderService",
            () -> new ManualOrderService(this.notificationSender(), this.priceCalculator())
        );
    }

    /**
     * Сколько объектов уже создано.
     * @return Число созданных объектов
     */
    public int createdCount() {
        return this.singletons.size();
    }

    // ленивое создание с кэшированием написано через get/put, а не computeIfAbsent:
    // фабрика зависимости трогает ту же карту рекурсивно, а computeIfAbsent
    // такого не допускает
    private NotificationSender sender() {
        final NotificationSender sender;
        if ("sms".equals(this.channel)) {
            sender = new SmsSender();
        } else {
            sender = new EmailSender();
        }
        return sender;
    }

    private Object singleton(final String name, final Supplier<Object> factory) {
        Object bean = this.singletons.get(name);
        if (bean == null) {
            bean = factory.get();
            this.singletons.put(name, bean);
        }
        return bean;
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.step2;

import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import ru.sprbut.m11.domain.SmsSender;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
public class ObjectFactory {

    /**
     * Синглтоны.
     */
    private final Map<String, Object> singletons = new HashMap<>();
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
     * @return Синглтон: создаём один раз, дальше отдаём тот же экземпляр
     */
    public NotificationSender notificationSender() {
        return (NotificationSender) this.singleton("sender",
                () -> "sms".equals(this.channel) ? new SmsSender() : new EmailSender());
    }

    /**
     * Цена.
     * @return Цена
     */
    public PriceCalculator priceCalculator() {
        return (PriceCalculator) this.singleton("calculator",
                () -> new PriceCalculator(new BigDecimal("0.20")));
    }

    /**
     * Сборка графа: сначала зависимости, потом сам объект.
     * @return Сборка графа: сначала зависимости, потом сам объект
     */
    public ManualOrderService orderService() {
        return (ManualOrderService) this.singleton("orderService",
                () -> new ManualOrderService(this.notificationSender(), this.priceCalculator()));
    }

    /**
     * Ленивое создание с кэшированием.
     *
     * <p>Написано через get/put, а не {@code computeIfAbsent}: фабрика зависимости
     * вызывает фабрику другого бина, то есть трогает ту же карту рекурсивно,
     * а {@code computeIfAbsent} такого не допускает — будет
     * {@code ConcurrentModificationException}. Контейнеру приходится решать
     * ровно эту задачу, только на графе произвольной глубины.</p>
     * @param factory Фабрика
     * @param name Имя
     * @return Ленивое создание с кэшированием
     */
    private Object singleton(final String name, final java.util.function.Supplier<Object> factory) {
        final Object existing = this.singletons.get(name);
        if (existing != null) {
            return existing;
        }
        final Object created = factory.get();
        this.singletons.put(name, created);
        return created;
    }

    /**
     * Количество.
     * @return Количество
     */
    public int createdCount() {
        return this.singletons.size();
    }
}

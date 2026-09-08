/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m11.step4;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import ru.sprbut.m11.domain.EmailSender;
import ru.sprbut.m11.domain.NotificationSender;
import ru.sprbut.m11.domain.PriceCalculator;
import ru.sprbut.m11.domain.SmsSender;
import ru.sprbut.m11.step2.ManualOrderService;

/**
 * Шаг 4: тот же граф, но собранный на этапе компиляции.
 *
 * <p>Модуль Dagger — это тот же {@link ru.sprbut.m11.step2.ObjectFactory}, только
 * без сборки: методы говорят, <b>как создать</b> каждый объект, а связать их между
 * собой обязан процессор аннотаций из модулей 07–09. Аргументы метода
 * {@link #orderService} — такие же точки внедрения, как в
 * {@link ru.sprbut.m11.step3.SpringWiringConfig}, но подставляет их не контейнер
 * в рантайме, а сгенерированный код.</p>
 *
 * <p>Отсюда и разница в цене ошибки: забытая привязка у Spring обнаруживается при
 * старте контекста, а здесь — при компиляции, вместе с остальными ошибками типов.
 * Платить за это приходится гибкостью: граф зафиксирован в момент сборки, и ни
 * профилей, ни условных бинов, ни подмены реализации по конфигурации тут нет.</p>
 *
 * <p>Классы домена остаются чистыми: {@code @Provides} для того и нужен, чтобы
 * привязывать типы, которые нельзя разметить {@code @Inject} — чужие или, как
 * здесь, принадлежащие соседнему шагу.</p>
 *
 * @since 1.0
 */
@Module
public final class WiringModule {

    /**
     * Канал.
     */
    private final String channel;

    /**
     * Основной конструктор.
     * @param channel Канал
     */
    public WiringModule(final String channel) {
        this.channel = channel;
    }

    /**
     * Выбор реализации — единственное решение, которое остаётся за человеком.
     * @return Отправитель уведомлений
     */
    @Provides
    @Singleton
    public NotificationSender notificationSender() {
        final NotificationSender sender;
        if ("sms".equals(this.channel)) {
            sender = new SmsSender();
        } else {
            sender = new EmailSender();
        }
        return sender;
    }

    /**
     * Цена.
     * @return Калькулятор цены
     */
    @Provides
    @Singleton
    public PriceCalculator priceCalculator() {
        return new PriceCalculator(new BigDecimal("0.20"));
    }

    /**
     * Аргументы этого метода процессор подберёт по типу и подставит в сгенерированном
     * коде — ни рефлексии, ни поиска бинов в рантайме здесь не будет.
     * @param sender Отправитель
     * @param calculator Калькулятор
     * @return Сервис, собранный сгенерированным кодом
     */
    @Provides
    @Singleton
    public ManualOrderService orderService(
        final NotificationSender sender, final PriceCalculator calculator
    ) {
        return new ManualOrderService(sender, calculator);
    }
}

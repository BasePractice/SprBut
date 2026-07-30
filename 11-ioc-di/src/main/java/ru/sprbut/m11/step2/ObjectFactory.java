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
 * <p>
 * Ручная сборка, вынесенная в одно место. Это уже прообраз контейнера:
 * фабрика знает, как создать объект, и хранит созданные синглтоны.
 * <p>
 * Хорошо видно, чего такой подход стоит: <b>каждую связь надо описать руками</b>.
 * Добавили зависимость в конструктор — правьте фабрику. На двух десятках
 * сервисов это превращается в отдельный неподъёмный класс — и ровно поэтому
 * появились IoC-контейнеры.
 */
public class ObjectFactory {

    private final Map<String, Object> singletons = new HashMap<>();
    private final String channel;

    public ObjectFactory(String channel) {
        this.channel = channel;
    }

    /** Синглтон: создаём один раз, дальше отдаём тот же экземпляр. */
    public NotificationSender notificationSender() {
        return (NotificationSender) singleton("sender",
                () -> "sms".equals(channel) ? new SmsSender() : new EmailSender());
    }

    public PriceCalculator priceCalculator() {
        return (PriceCalculator) singleton("calculator",
                () -> new PriceCalculator(new BigDecimal("0.20")));
    }

    /** Сборка графа: сначала зависимости, потом сам объект. */
    public ManualOrderService orderService() {
        return (ManualOrderService) singleton("orderService",
                () -> new ManualOrderService(notificationSender(), priceCalculator()));
    }

    /**
     * Ленивое создание с кэшированием.
     * <p>
     * Написано через get/put, а не {@code computeIfAbsent}: фабрика зависимости
     * вызывает фабрику другого бина, то есть трогает ту же карту рекурсивно,
     * а {@code computeIfAbsent} такого не допускает — будет
     * {@code ConcurrentModificationException}. Контейнеру приходится решать
     * ровно эту задачу, только на графе произвольной глубины.
     */
    private Object singleton(String name, java.util.function.Supplier<Object> factory) {
        Object existing = singletons.get(name);
        if (existing != null) {
            return existing;
        }
        Object created = factory.get();
        singletons.put(name, created);
        return created;
    }

    public int createdCount() {
        return singletons.size();
    }
}

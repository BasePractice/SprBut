package ru.sprbut.m23.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * Журнал аудита — singleton, который переживает все запросы.
 * <p>
 * {@code CopyOnWriteArrayList} выбран не случайно: бин один на всё приложение,
 * а писать в него будут потоки веб-сервера. Обычный {@code ArrayList} здесь
 * тихо ломается под нагрузкой — классическая цена того, что областью видимости
 * по умолчанию является singleton.
 */
@Component
public final class AuditTrail {

    private final List<String> records;

    public AuditTrail() {
        this(new CopyOnWriteArrayList<>());
    }

    public AuditTrail(List<String> records) {
        this.records = records;
    }

    /**
     * Добавляет запись об операции.
     */
    public void record(String operation) {
        this.records.add(operation);
    }

    /**
     * Снимок журнала на текущий момент.
     */
    public List<String> records() {
        return List.copyOf(this.records);
    }
}

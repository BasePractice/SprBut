package ru.sprbut.m08.service;

import ru.sprbut.m07.api.Registered;
import ru.sprbut.m07.api.Todo;

import java.util.ArrayList;
import java.util.List;

/**
 * Третий участник реестра, он же носитель {@code @Todo} — при сборке модуля
 * javac напечатает предупреждение от {@code TodoProcessor}.
 */
@Registered("audit")
public class AuditLog {

    private final List<String> entries = new ArrayList<>();

    @Todo("заменить на структурированное логирование")
    public void record(String event) {
        entries.add(event);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}

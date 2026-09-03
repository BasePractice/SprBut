/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Хранилище заметок в памяти.
 *
 * <p>Реальной базы здесь нет намеренно: тема раздела — путь запроса от сокета
 * до метода контроллера, и всё, что стоит за контроллером, должно быть
 * настолько простым, чтобы не отвлекать.</p>
 *
 * @since 1.0
 */
@Component
public final class Notes {

    /**
     * Заметки по номерам.
     */
    private final Map<Long, Note> kept;

    /**
     * Счётчик номеров.
     */
    private final AtomicLong last;

    /**
     * Основной конструктор.
     */
    public Notes() {
        this.kept = new ConcurrentSkipListMap<>();
        this.last = new AtomicLong();
    }

    /**
     * Все заметки в порядке появления.
     * @return Все заметки в порядке появления
     */
    public List<Note> all() {
        return List.copyOf(this.kept.values());
    }

    /**
     * Заметка по номеру, если она есть.
     * @param id Номер заметки
     * @return Заметка по номеру, если она есть
     */
    public Optional<Note> find(final long id) {
        return Optional.ofNullable(this.kept.get(id));
    }

    /**
     * Заметки, чей текст содержит подстроку.
     * @param text Искомая подстрока
     * @return Заметки, чей текст содержит подстроку
     */
    public List<Note> search(final String text) {
        return this.kept.values().stream()
            .filter(note -> note.text().contains(text))
            .toList();
    }

    /**
     * Новая заметка с очередным номером.
     * @param text Текст заметки
     * @return Новая заметка с очередным номером
     */
    public Note add(final String text) {
        final Note note = new Note(this.last.incrementAndGet(), text);
        this.kept.put(note.id(), note);
        return note;
    }
}

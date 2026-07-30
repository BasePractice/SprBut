package ru.sprbut.m02.extended;

import java.util.List;

/**
 * Итог биндинга: заполненный объект и отчёт о том, что легло, а что нет.
 * <p>
 * Список пропущенных ключей здесь не для полноты. Неизвестный ключ в конфигурации —
 * почти всегда опечатка в имени свойства, и единственный способ её заметить —
 * увидеть ключ, который никуда не подошёл.
 *
 * @param bean       заполненный объект
 * @param bound      свойства, которые удалось привязать
 * @param ignored    ключи, которым не нашлось свойства
 */
public record Binding<T>(T bean, List<String> bound, List<String> ignored) {

    public Binding {
        bound = List.copyOf(bound);
        ignored = List.copyOf(ignored);
    }
}

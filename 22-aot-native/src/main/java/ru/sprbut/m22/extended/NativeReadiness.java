package ru.sprbut.m22.extended;

import java.util.Collection;
import java.util.List;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

/**
 * <b>Расширенный пример модуля 22.</b>
 * <p>
 * Готовность приложения к native image: сверяет классы, которые создаются
 * рефлексией, с тем, что реально попало в {@code RuntimeHints}.
 * <p>
 * Смысл упражнения в том, что обычные тесты такую дыру не видят. На JVM
 * незарегистрированный класс работает как ни в чём не бывало; сборка native
 * тоже проходит успешно — падает уже готовый образ, в рантайме, на клиенте.
 * Единственный способ поймать это заранее — проверять подсказки, а не поведение.
 * <p>
 * Тот же приём использует {@code RuntimeHintsPredicates} в тестах самого Spring.
 */
public final class NativeReadiness {

    private final RuntimeHints hints;

    public NativeReadiness(RuntimeHints hints) {
        this.hints = hints;
    }

    /**
     * Переживёт ли создание этого класса рефлексией сборку в native image.
     */
    public boolean covers(Class<?> type) {
        return RuntimeHintsPredicates.reflection()
            .onType(type)
            .test(this.hints);
    }

    /**
     * Классы, которые рефлексия использует, а подсказки не упоминают —
     * список будущих отказов в рантайме образа.
     */
    public List<String> gaps(Collection<Class<?>> types) {
        return types.stream()
            .filter(type -> !covers(type))
            .map(Class::getName)
            .toList();
    }
}

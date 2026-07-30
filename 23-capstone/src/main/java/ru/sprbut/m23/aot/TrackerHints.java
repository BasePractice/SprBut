package ru.sprbut.m23.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import ru.sprbut.m23.web.NewTaskRequest;
import ru.sprbut.m23.web.TaskView;

/**
 * Подсказки для native image.
 * <p>
 * Классы DTO нигде не создаются явно: их собирает Jackson рефлексией по данным
 * запроса. Для графа достижимости GraalVM это невидимая связь, и без объявления
 * образ соберётся, запустится и упадёт на первом же обращении к API.
 */
public final class TrackerHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classes) {
        hints.reflection()
            .registerType(NewTaskRequest.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(TaskView.class, MemberCategory.INVOKE_DECLARED_METHODS);
    }
}

package ru.sprbut.m22.hints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import ru.sprbut.m22.reflection.CsvPlugin;

/**
 * Слайд «AOT и native image»: {@code RuntimeHints} для остатков рефлексии.
 * <p>
 * Подсказка — это обещание сборщику: «этот класс понадобится, хотя ссылок на него
 * ты не найдёшь». {@link MemberCategory#INVOKE_DECLARED_CONSTRUCTORS} добавляет
 * к самому классу ещё и его конструкторы, без которых {@code newInstance}
 * упадёт уже в рантайме образа.
 * <p>
 * Регистратор подключается через {@code @ImportRuntimeHints} на конфигурации —
 * Spring вызовет его во время AOT-обработки при сборке, а не при старте.
 */
public final class PluginHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classes) {
        hints.reflection()
            .registerType(CsvPlugin.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.resources().registerPattern("plugins/*.properties");
    }
}

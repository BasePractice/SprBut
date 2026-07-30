package ru.sprbut.m04;

/**
 * Слайд 31: что модульная система разрешает делать с чужим классом.
 * <p>
 * JPMS различает два уровня, и путать их дорого:
 * <ul>
 *   <li><b>exports</b> — типы пакета видны компилятору и обычному коду;</li>
 *   <li><b>opens</b> — пакет дополнительно открыт для <i>глубокой рефлексии</i>,
 *       то есть {@code setAccessible(true)} на его закрытых членах разрешён.</li>
 * </ul>
 * Модуль {@code java.base} экспортирует почти всё, а открывает почти ничего.
 * Отсюда и требование Spring, Hibernate и Jackson к флагам вида
 * {@code --add-opens java.base/java.lang=ALL-UNNAMED}.
 */
public final class ModuleAccess {

    private final Class<?> type;

    public ModuleAccess(Class<?> type) {
        this.type = type;
    }

    /**
     * Открыт ли пакет для глубокой рефлексии из нашего модуля.
     * Штатный способ проверить доступ, не ловя исключение.
     */
    public boolean open() {
        return this.type.getModule()
            .isOpen(this.type.getPackageName(), ModuleAccess.class.getModule());
    }

    /**
     * Экспортирован ли пакет — то есть виден ли он обычному коду.
     */
    public boolean exported() {
        return this.type.getModule()
            .isExported(this.type.getPackageName(), ModuleAccess.class.getModule());
    }

    /**
     * Имя модуля-владельца. Код, запущенный с classpath, попадает
     * в безымянный модуль, и здесь будет {@code null}.
     */
    public String moduleName() {
        return this.type.getModule().getName();
    }
}

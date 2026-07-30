package ru.sprbut.m01;

import java.lang.reflect.Field;

/**
 * Слайд 7: «Получить и задать значение полей, в том числе с модификатором private».
 * <p>
 * Ключевой вызов — {@code setAccessible(true)}: он снимает проверку доступа.
 * Именно так Spring внедряет зависимости в поля с {@code @Autowired},
 * а Hibernate заполняет сущности, не требуя сеттеров.
 */
public final class FieldAccessor {

    private FieldAccessor() {
    }

    /**
     * Читает значение поля, даже если оно private.
     *
     * @param target    объект, у которого читаем поле
     * @param fieldName имя поля
     */
    public static Object read(Object target, String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Не удалось прочитать поле " + fieldName, e);
        }
    }

    /**
     * Записывает значение в поле, даже если оно private.
     */
    public static void write(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Не удалось записать поле " + fieldName, e);
        }
    }

    /**
     * Значение статического поля читается без экземпляра — {@code get(null)}.
     */
    public static Object readStatic(Class<?> type, String fieldName) {
        Field field = findField(type, fieldName);
        field.setAccessible(true);
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Не удалось прочитать статическое поле " + fieldName, e);
        }
    }

    /**
     * Поиск поля с подъёмом по иерархии наследования: {@code getDeclaredField}
     * смотрит только в самом классе, поэтому родителей обходим вручную.
     */
    public static Field findField(Class<?> type, String fieldName) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // поле объявлено выше по иерархии — продолжаем подъём
            }
        }
        throw new IllegalArgumentException("Поле '" + fieldName + "' не найдено в " + type.getName());
    }
}

package ru.sprbut.m01;

import java.lang.reflect.Field;

/**
 * Слайд 7: «Получить и задать значение полей, в том числе с модификатором private».
 * <p>
 * Одно поле одного объекта, доступное на чтение и запись независимо от того,
 * что написано в его модификаторах. Ключевой вызов — {@code setAccessible(true)}:
 * он снимает проверку доступа.
 * <p>
 * Именно так Spring внедряет зависимости в поля с {@code @Autowired},
 * а Hibernate заполняет сущности, не требуя сеттеров. И так же рефлексия
 * пишет в {@code private final} поле, у которого никакого сеттера нет
 * и быть не может.
 */
public final class ObjectField {

    private final Object target;

    private final String name;

    public ObjectField(Object target, String name) {
        this.target = target;
        this.name = name;
    }

    /**
     * Значение поля.
     */
    public Object value() {
        Field field = declaration();
        field.setAccessible(true);
        try {
            return field.get(this.target);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Не удалось прочитать поле " + this.name, denied);
        }
    }

    /**
     * Записывает значение в поле в обход сеттера и модификатора доступа.
     */
    public void assign(Object value) {
        Field field = declaration();
        field.setAccessible(true);
        try {
            field.set(this.target, value);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Не удалось записать поле " + this.name, denied);
        }
    }

    /**
     * Объявление поля — найденное с подъёмом по иерархии наследования.
     */
    public Field declaration() {
        return new Declared(this.target.getClass()).field(this.name);
    }
}

package ru.sprbut.m02.classic;

import java.lang.reflect.Constructor;

/**
 * Слайд 18: «Избыточность и мутабельность».
 * <p>
 * Бин, созданный конструктором без параметров, — то есть пустой и потому
 * заведомо невалидный. Между созданием и последним сеттером объект находится
 * в состоянии, которого предметная область не допускает, и с этим ничего
 * нельзя сделать: соглашение требует именно такого порядка.
 * <p>
 * Это и есть главный аргумент в пользу {@code record} и билдеров из модуля 10.
 */
public final class EmptyBean {

    private final Class<?> type;

    public EmptyBean(Class<?> type) {
        this.type = type;
    }

    /**
     * Свежесозданный пустой экземпляр.
     */
    public Object instance() {
        try {
            Constructor<?> constructor = this.type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(
                "Не удалось создать " + this.type.getName() + " конструктором без параметров",
                failure
            );
        }
    }
}

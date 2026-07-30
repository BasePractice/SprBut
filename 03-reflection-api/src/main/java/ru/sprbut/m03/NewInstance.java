package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Объект, созданный рефлексией под заданные аргументы.
 * <p>
 * Три разных отказа разведены намеренно: «нет подходящего конструктора»,
 * «тип нельзя инстанцировать» и «конструктор сам бросил исключение» —
 * это три разные ошибки, и общее сообщение про «не удалось создать»
 * не помогло бы ни в одном из случаев.
 */
public final class NewInstance {

    private final Class<?> type;

    private final Object[] args;

    public NewInstance(Class<?> type, Object... args) {
        this.type = type;
        this.args = args.clone();
    }

    /**
     * Созданный объект.
     */
    public Object object() {
        Constructor<?> chosen = new Constructors(this.type).matching(this.args)
            .orElseThrow(() -> new IllegalArgumentException(
                "Нет конструктора " + this.type.getSimpleName()
                    + " под аргументы " + Arrays.toString(this.args)
            ));
        chosen.setAccessible(true);
        try {
            return chosen.newInstance(this.args);
        } catch (InstantiationException abstractType) {
            throw new IllegalStateException(
                "Нельзя создать экземпляр " + this.type.getSimpleName()
                    + " — абстрактный класс или интерфейс",
                abstractType
            );
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к конструктору", denied);
        } catch (InvocationTargetException wrapped) {
            Throwable cause = wrapped.getCause();
            if (cause instanceof RuntimeException unchecked) {
                throw unchecked;
            }
            throw new IllegalStateException(cause);
        }
    }
}

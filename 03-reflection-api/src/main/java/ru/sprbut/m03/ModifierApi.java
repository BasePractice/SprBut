package ru.sprbut.m03;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайд 25 (СХЕМА 1): {@link Modifier} — расшифровка битовой маски.
 * <p>
 * {@code getModifiers()} у {@code Class}, {@code Field}, {@code Method} и
 * {@code Constructor} возвращает один и тот же {@code int}. Это не enum и не набор
 * объектов — просто набор битов, поэтому проверки складываются побитовым «И».
 */
public final class ModifierApi {

    private ModifierApi() {
    }

    /** Все взведённые флаги одного члена класса — списком имён. */
    public static List<String> flagsOf(Member member) {
        return flags(member.getModifiers());
    }

    public static List<String> flags(int modifiers) {
        List<String> result = new ArrayList<>();
        if (Modifier.isPublic(modifiers)) {
            result.add("public");
        }
        if (Modifier.isProtected(modifiers)) {
            result.add("protected");
        }
        if (Modifier.isPrivate(modifiers)) {
            result.add("private");
        }
        if (Modifier.isStatic(modifiers)) {
            result.add("static");
        }
        if (Modifier.isFinal(modifiers)) {
            result.add("final");
        }
        if (Modifier.isAbstract(modifiers)) {
            result.add("abstract");
        }
        if (Modifier.isSynchronized(modifiers)) {
            result.add("synchronized");
        }
        if (Modifier.isVolatile(modifiers)) {
            result.add("volatile");
        }
        if (Modifier.isTransient(modifiers)) {
            result.add("transient");
        }
        if (Modifier.isNative(modifiers)) {
            result.add("native");
        }
        return result;
    }

    /**
     * Package-private не имеет собственного бита: это отсутствие трёх других.
     * Частая ловушка при написании сканеров классов.
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers)
                && !Modifier.isPrivate(modifiers);
    }

    /** Ровно то, что печатает {@code javap}: модификаторы в каноническом порядке. */
    public static String describe(int modifiers) {
        return Modifier.toString(modifiers);
    }

    /** Маска модификаторов, допустимых для класса — {@code volatile} в неё не входит. */
    public static boolean isValidForClass(int modifier) {
        return (modifier & Modifier.classModifiers()) == modifier;
    }

    /** Маска модификаторов, допустимых для поля. */
    public static boolean isValidForField(int modifier) {
        return (modifier & Modifier.fieldModifiers()) == modifier;
    }
}

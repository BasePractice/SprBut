package ru.sprbut.m02.classic;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Слайды 12–16: подчиняется ли класс соглашению JavaBeans.
 * <p>
 * Проверка написана на рефлексии из модуля 01 — тем же способом, каким Spring
 * и Hibernate решают, умеют ли они работать с типом.
 * <p>
 * Требование {@link Serializable} необязательно: слайд прямо оговаривает,
 * что Spring его не спрашивает. Поэтому строгость вынесена в конструктор —
 * один и тот же класс отвечает на оба вопроса.
 */
public final class BeanVerdict {

    private final Class<?> type;

    private final boolean serializable;

    public BeanVerdict(Class<?> type) {
        this(type, false);
    }

    public BeanVerdict(Class<?> type, boolean serializable) {
        this.type = type;
        this.serializable = serializable;
    }

    /**
     * Подчиняется ли класс соглашению.
     */
    public boolean valid() {
        return violations().isEmpty();
    }

    /**
     * Нарушения соглашения, каждое одним предложением.
     */
    public List<String> violations() {
        List<String> found = new ArrayList<>();
        if (!constructible()) {
            found.add("нет публичного конструктора без параметров");
        }
        BeanProperties properties = new BeanProperties(this.type);
        for (String property : properties.writable()) {
            if (properties.reader(property) == null) {
                found.add("у свойства '" + property + "' есть setter, но нет getter");
            }
        }
        if (this.serializable && !Serializable.class.isAssignableFrom(this.type)) {
            found.add("класс не реализует Serializable");
        }
        return List.copyOf(found);
    }

    /**
     * Первое требование соглашения: {@code public Xxx()}. Без него контейнер
     * не сможет создать объект дефолтным способом.
     */
    public boolean constructible() {
        for (Constructor<?> candidate : this.type.getConstructors()) {
            if (candidate.getParameterCount() == 0 && Modifier.isPublic(candidate.getModifiers())) {
                return true;
            }
        }
        return false;
    }
}

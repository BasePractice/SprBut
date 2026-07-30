package ru.sprbut.m21.extended;

import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Поломка контекста, умеющая назвать себя.
 * <p>
 * Настоящая причина у Spring лежит не наверху: в вершине стека почти всегда
 * {@code UnsatisfiedDependencyException} со ссылкой на бин, а интересное —
 * этажами ниже. Поэтому разбор идёт по цепочке {@code getCause()} до первого
 * знакомого типа.
 * <p>
 * Порядок проверок важен: {@code NoUniqueBeanDefinitionException} наследует
 * {@code NoSuchBeanDefinitionException}, и перепутанный порядок превратил бы
 * «бинов слишком много» в «бина нет».
 */
public final class Failure implements Diagnosis {

    private final Throwable thrown;

    public Failure(Throwable thrown) {
        this.thrown = thrown;
    }

    @Override
    public String summary() {
        return diagnosis().summary();
    }

    @Override
    public String remedy() {
        return diagnosis().remedy();
    }

    /**
     * Разбор цепочки причин до первой узнаваемой ошибки контейнера.
     */
    public Diagnosis diagnosis() {
        for (Throwable cause = this.thrown; cause != null; cause = cause.getCause()) {
            if (cause instanceof NoUniqueBeanDefinitionException unique) {
                return new AmbiguousBean(unique);
            }
            if (cause instanceof BeanCurrentlyInCreationException circular) {
                return new CircularReference(circular);
            }
            if (cause instanceof NoSuchBeanDefinitionException missing) {
                return new MissingBean(missing);
            }
            if (cause.getCause() == cause) {
                break;
            }
        }
        return new UnknownFailure(this.thrown);
    }
}

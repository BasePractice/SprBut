package ru.sprbut.m21.extended;

import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз для {@code BeanCurrentlyInCreationException}: бин потребовал сам себя
 * через цепочку зависимостей и застрял в полусобранном состоянии.
 */
public final class CircularReference implements Diagnosis {

    private final BeanCurrentlyInCreationException cause;

    public CircularReference(BeanCurrentlyInCreationException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return "циклическая зависимость замкнулась на бине " + this.cause.getBeanName();
    }

    @Override
    public String remedy() {
        return "разделить бины, вынеся общую ответственность в третий,"
            + " либо отложить одну сторону через @Lazy";
    }
}

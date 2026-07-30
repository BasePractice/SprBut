package ru.sprbut.m21.extended;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз для {@code NoSuchBeanDefinitionException}: запрошенного типа
 * в контейнере нет ни в одном экземпляре.
 */
public final class MissingBean implements Diagnosis {

    private final NoSuchBeanDefinitionException cause;

    public MissingBean(NoSuchBeanDefinitionException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return "в контексте нет бина типа " + type();
    }

    @Override
    public String remedy() {
        return "объявить @Bean или @Component для " + type()
            + ", добавить пакет в @ComponentScan либо сделать зависимость Optional";
    }

    private String type() {
        return this.cause.getResolvableType() == null
            ? String.valueOf(this.cause.getBeanName())
            : this.cause.getResolvableType().toClass().getSimpleName();
    }
}

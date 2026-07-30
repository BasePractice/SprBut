package ru.sprbut.m21.extended;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import ru.sprbut.m21.Diagnosis;

/**
 * Диагноз для {@code NoUniqueBeanDefinitionException}: кандидатов больше одного,
 * а точка внедрения не сказала, какой ей нужен.
 */
public final class AmbiguousBean implements Diagnosis {

    private final NoUniqueBeanDefinitionException cause;

    public AmbiguousBean(NoUniqueBeanDefinitionException cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return "на одну точку внедрения нашлось несколько бинов: " + candidates();
    }

    @Override
    public String remedy() {
        return "пометить обычную реализацию @Primary либо назвать нужную"
            + " через @Qualifier в точке внедрения";
    }

    /**
     * Имена бинов-кандидатов через запятую — это и есть первая подсказка,
     * куда смотреть.
     */
    public String candidates() {
        return this.cause.getBeanNamesFound() == null
            ? "неизвестно"
            : String.join(", ", this.cause.getBeanNamesFound());
    }
}

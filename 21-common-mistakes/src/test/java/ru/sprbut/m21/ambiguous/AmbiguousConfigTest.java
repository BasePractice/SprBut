package ru.sprbut.m21.ambiguous;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд «Типичные ошибки»: NoUniqueBeanDefinitionException")
final class AmbiguousConfigTest {

    @Test
    @DisplayName("два кандидата на одну точку внедрения останавливают контекст")
    void dontGuessBetweenTwoCandidates() {
        assertThat(
            "ambiguous injection point cannot stop the context",
            assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(AmbiguousConfig.class).close()
            ).getMostSpecificCause(),
            instanceOf(NoUniqueBeanDefinitionException.class)
        );
    }

    @Test
    @DisplayName("контейнер называет обоих кандидатов поимённо")
    void namesBothCandidates() {
        NoUniqueBeanDefinitionException cause = (NoUniqueBeanDefinitionException) assertThrows(
            BeanCreationException.class,
            () -> new AnnotationConfigApplicationContext(AmbiguousConfig.class).close()
        ).getMostSpecificCause();
        assertThat(
            "ambiguity report cannot list both bean names",
            cause.getBeanNamesFound(),
            containsInAnyOrder("express", "economy")
        );
    }

    @Test
    @DisplayName("@Primary выбирает реализацию по умолчанию за все точки внедрения")
    void resolvesByPrimary() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(PrimaryConfig.class)) {
            assertThat(
                "primary shipper cannot win the default injection point",
                context.getBean(DeliveryService.class).promise(),
                equalTo(1)
            );
        }
    }

    @Test
    @DisplayName("@Qualifier выбирает реализацию на стороне потребителя")
    void resolvesByQualifier() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(QualifierConfig.class)) {
            assertThat(
                "qualified injection point cannot pick the economy shipper",
                context.getBean(EconomyDelivery.class).promise(),
                equalTo(7)
            );
        }
    }
}

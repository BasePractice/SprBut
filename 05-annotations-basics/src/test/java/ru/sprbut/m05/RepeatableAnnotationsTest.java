package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.declarations.Schedule;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайд 44: @Repeatable и аннотация-контейнер")
class RepeatableAnnotationsTest {

    private Method job(String name) throws NoSuchMethodException {
        return RepeatableAnnotations.Jobs.class.getMethod(name);
    }

    @Test
    @DisplayName("Одно вхождение: в байткоде лежит сама аннотация")
    void singleOccurrenceStaysItself() throws NoSuchMethodException {
        Method hourly = job("hourly");

        assertThat(RepeatableAnnotations.rawAnnotationNames(hourly)).containsExactly("Schedule");
        assertThat(RepeatableAnnotations.naiveSingleSchedule(hourly)).isPresent();
        assertThat(RepeatableAnnotations.allSchedules(hourly)).hasSize(1);
    }

    @Test
    @DisplayName("Два вхождения: компилятор заворачивает их в контейнер Schedules")
    void multipleOccurrencesBecomeAContainer() throws NoSuchMethodException {
        Method twice = job("twiceADay");

        assertThat(RepeatableAnnotations.rawAnnotationNames(twice)).containsExactly("Schedules");
        assertThat(RepeatableAnnotations.container(twice)).isPresent();
    }

    @Test
    @DisplayName("Ловушка: getAnnotation при двух вхождениях возвращает null")
    void naiveReadBreaksOnMultipleOccurrences() throws NoSuchMethodException {
        assertThat(RepeatableAnnotations.naiveSingleSchedule(job("twiceADay")))
                .as("аннотации в байткоде нет — там контейнер")
                .isEmpty();
    }

    @Test
    @DisplayName("getAnnotationsByType работает одинаково для 0, 1 и N вхождений")
    void getAnnotationsByTypeAlwaysWorks() throws NoSuchMethodException {
        assertThat(RepeatableAnnotations.allSchedules(job("notScheduled"))).isEmpty();
        assertThat(RepeatableAnnotations.allSchedules(job("hourly"))).hasSize(1);
        assertThat(RepeatableAnnotations.allSchedules(job("twiceADay"))).hasSize(2);
    }

    @Test
    @DisplayName("Значения читаются в порядке объявления, defaults подставляются")
    void readsValuesInDeclarationOrder() throws NoSuchMethodException {
        assertThat(RepeatableAnnotations.cronExpressions(job("twiceADay")))
                .containsExactly("0 0 3 * * *", "0 0 15 * * *");
        assertThat(RepeatableAnnotations.allSchedules(job("hourly")))
                .extracting(Schedule::zone)
                .containsExactly("UTC");
    }

    @Test
    @DisplayName("Явно написанный контейнер даёт тот же результат, что и повторение")
    void explicitContainerIsEquivalent() throws NoSuchMethodException {
        assertThat(RepeatableAnnotations.cronExpressions(job("explicitContainer")))
                .containsExactly("0 */5 * * * *", "0 */10 * * * *");
    }
}

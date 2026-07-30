package ru.sprbut.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m05.samples.Jobs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("Слайд 42: @Repeatable — сахар поверх контейнера")
final class RepeatedTest {

    @Test
    @DisplayName("одно вхождение читается обоими способами одинаково")
    void readsSingleOccurrence() throws NoSuchMethodException {
        assertThat(
            "single occurrence cannot be read the naive way",
            new Repeated(Jobs.class.getMethod("hourly")).single().isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("при двух вхождениях наивный getAnnotation возвращает пустоту")
    void dontReadSeveralOccurrencesNaively() throws NoSuchMethodException {
        assertThat(
            "naive lookup cannot fail on several occurrences",
            new Repeated(Jobs.class.getMethod("twiceADay")).single().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("getAnnotationsByType разворачивает контейнер прозрачно")
    void readsAllOccurrences() throws NoSuchMethodException {
        assertThat(
            "getAnnotationsByType cannot unwrap the container",
            new Repeated(Jobs.class.getMethod("twiceADay")).all(),
            hasSize(2)
        );
    }

    @Test
    @DisplayName("при двух вхождениях в байткоде лежит контейнер")
    void keepsContainerInBytecode() throws NoSuchMethodException {
        assertThat(
            "container cannot appear in the bytecode",
            new Repeated(Jobs.class.getMethod("twiceADay")).container().isPresent(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("при одном вхождении контейнера нет")
    void dontCreateContainerForSingle() throws NoSuchMethodException {
        assertThat(
            "single occurrence cannot avoid the container",
            new Repeated(Jobs.class.getMethod("hourly")).container().isEmpty(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("контейнер, написанный вручную, ведёт себя точно так же")
    void treatsExplicitContainerAlike() throws NoSuchMethodException {
        assertThat(
            "explicit container cannot behave like the generated one",
            new Repeated(Jobs.class.getMethod("explicitContainer")).all(),
            hasSize(2)
        );
    }

    @Test
    @DisplayName("значения элементов читаются из каждого вхождения")
    void readsEveryValue() throws NoSuchMethodException {
        assertThat(
            "each occurrence cannot yield its own value",
            new Repeated(Jobs.class.getMethod("twiceADay")).crons(),
            contains("0 0 3 * * *", "0 0 15 * * *")
        );
    }

    @Test
    @DisplayName("метод без аннотации даёт пустой список, а не null")
    void reportsEmptyForUnannotatedMethod() throws NoSuchMethodException {
        assertThat(
            "unannotated method cannot yield an empty list",
            new Repeated(Jobs.class.getMethod("notScheduled")).all(),
            emptyIterable()
        );
    }
}

package ru.sprbut.m14.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m14.LifecycleConfig;
import ru.sprbut.m14.LifecycleLog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("Расширенный пример: шкала жизненного цикла с проверкой инвариантов")
final class LifecycleTimelineTest {

    private static LifecycleTimeline started() {
        new LifecycleLog().clear();
        new AnnotationConfigApplicationContext(LifecycleConfig.class).close();
        return new LifecycleTimeline();
    }

    @Test
    @DisplayName("журнал разбирается в шаги с номерами и фазами")
    void parsesLogIntoSteps() {
        assertThat(
            "log cannot be parsed into numbered steps",
            started().of("managedBean").get(0).phase(),
            equalTo("constructor")
        );
    }

    @Test
    @DisplayName("номер шага берётся из первой цифры события")
    void readsStepNumber() {
        assertThat(
            "step number cannot be read from the event",
            started().of("managedBean").get(0).number(),
            equalTo(1)
        );
    }

    @Test
    @DisplayName("порядок шагов управляемого бина инвариантов не нарушает")
    void keepsInvariantsForManagedBean() {
        assertThat(
            "correct lifecycle cannot pass the invariants",
            started().violations("managedBean"),
            emptyIterable()
        );
    }

    @Test
    @DisplayName("отсутствие бина в журнале — тоже нарушение, а не пустой результат")
    void reportsMissingBean() {
        assertThat(
            "missing bean cannot be reported as a violation",
            started().violations("нетТакогоБина"),
            hasSize(1)
        );
    }

    @Test
    @DisplayName("шкала печатается по шагам, с именем бина в заголовке")
    void rendersTimeline() {
        assertThat(
            "timeline cannot be rendered with the bean name",
            started().render("managedBean"),
            containsString("Жизненный цикл 'managedBean'")
        );
    }

    @Test
    @DisplayName("сводка считает шаги каждого бина")
    void countsStepsPerBean() {
        assertThat(
            "summary cannot count the steps of every bean",
            started().summary(),
            hasKey("managedBean")
        );
    }

    @Test
    @DisplayName("управляемый бин доходит до фазы уничтожения")
    void reachesDestruction() {
        assertThat(
            "singleton bean cannot reach the destruction phase",
            started().destroyed("managedBean"),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("шагов у полноценного бина больше, чем у prototype")
    void countsMoreStepsForSingleton() {
        LifecycleTimeline timeline = started();
        assertThat(
            "singleton cannot pass more steps than a prototype",
            timeline.of("managedBean").size(),
            greaterThan(timeline.of("prototypeWithDestroy").size())
        );
    }
}

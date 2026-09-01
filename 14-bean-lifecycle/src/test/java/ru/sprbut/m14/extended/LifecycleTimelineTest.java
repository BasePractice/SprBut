/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m14.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m14.LifecycleConfig;
import ru.sprbut.m14.LifecycleLog;

/**
 * Расширенный пример: шкала жизненного цикла с проверкой инвариантов.
 * @since 1.0
 */
@DisplayName("Расширенный пример: шкала жизненного цикла с проверкой инвариантов")
final class LifecycleTimelineTest {

    @Test
    @DisplayName("журнал разбирается в шаги с номерами и фазами")
    void parsesLogIntoSteps() {
        MatcherAssert.assertThat(
            "log cannot be parsed into numbered steps",
            started().of("managedBean").get(0).phase(),
            Matchers.equalTo("constructor")
        );
    }

    @Test
    @DisplayName("номер шага берётся из первой цифры события")
    void readsStepNumber() {
        MatcherAssert.assertThat(
            "step number cannot be read from the event",
            started().of("managedBean").get(0).number(),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("порядок шагов управляемого бина инвариантов не нарушает")
    void keepsInvariantsForManagedBean() {
        MatcherAssert.assertThat(
            "correct lifecycle cannot pass the invariants",
            started().violations("managedBean"),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("отсутствие бина в журнале — тоже нарушение, а не пустой результат")
    void reportsMissingBean() {
        MatcherAssert.assertThat(
            "missing bean cannot be reported as a violation",
            started().violations("нетТакогоБина"),
            Matchers.hasSize(1)
        );
    }

    @Test
    @DisplayName("шкала печатается по шагам, с именем бина в заголовке")
    void rendersTimeline() {
        MatcherAssert.assertThat(
            "timeline cannot be rendered with the bean name",
            started().render("managedBean"),
            Matchers.containsString("Жизненный цикл 'managedBean'")
        );
    }

    @Test
    @DisplayName("сводка считает шаги каждого бина")
    void countsStepsPerBean() {
        MatcherAssert.assertThat(
            "summary cannot count the steps of every bean",
            started().summary(),
            Matchers.hasKey("managedBean")
        );
    }

    @Test
    @DisplayName("управляемый бин доходит до фазы уничтожения")
    void reachesDestruction() {
        MatcherAssert.assertThat(
            "singleton bean cannot reach the destruction phase",
            started().destroyed("managedBean"),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("шагов у полноценного бина больше, чем у prototype")
    void countsMoreStepsForSingleton() {
        final LifecycleTimeline timeline = started();
        MatcherAssert.assertThat(
            "singleton cannot pass more steps than a prototype",
            timeline.of("managedBean").size(),
            Matchers.greaterThan(timeline.of("prototypeWithDestroy").size())
        );
    }

    private static LifecycleTimeline started() {
        new LifecycleLog().clear();
        new AnnotationConfigApplicationContext(LifecycleConfig.class).close();
        return new LifecycleTimeline();
    }
}

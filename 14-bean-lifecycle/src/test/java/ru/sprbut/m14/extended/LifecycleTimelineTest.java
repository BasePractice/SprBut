package ru.sprbut.m14.extended;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m14.LifecycleConfig;
import ru.sprbut.m14.LifecycleLog;
import ru.sprbut.m14.ManagedBean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: временная шкала и проверка инвариантов")
class LifecycleTimelineTest {

    @BeforeEach
    void clearLog() {
        LifecycleLog.clear();
    }

    private void runFullLifecycle() {
        var context = new AnnotationConfigApplicationContext(LifecycleConfig.class);
        context.getBean(ManagedBean.class);
        context.getBean(LifecycleConfig.PrototypeWithDestroy.class);
        context.close();
    }

    @Test
    @DisplayName("Шкала собирается из журнала и содержит все фазы бина")
    void buildsTimeline() {
        runFullLifecycle();

        assertThat(LifecycleTimeline.stepsOf("managedBean"))
                .extracting(LifecycleTimeline.Step::phase)
                .containsExactly("constructor", "dependencies",
                        "aware-beanName", "aware-beanFactory", "aware-applicationContext",
                        "bpp-before", "postConstruct", "afterPropertiesSet", "bpp-after",
                        "preDestroy", "destroy");
    }

    @Test
    @DisplayName("Номера шагов идут по возрастанию — контракт не нарушен")
    void stepNumbersNeverDecrease() {
        runFullLifecycle();

        assertThat(LifecycleTimeline.validate("managedBean")).isEmpty();
    }

    @Test
    @DisplayName("Наглядный вывод пригоден для отладки")
    void rendersReadableTimeline() {
        runFullLifecycle();

        assertThat(LifecycleTimeline.render("managedBean"))
                .startsWith("Жизненный цикл 'managedBean':")
                .contains("1. constructor → managedBean")
                .contains("8. destroy → managedBean");
    }

    @Test
    @DisplayName("Инварианты порядка проверяются попарно")
    void checksPairwiseInvariants() {
        runFullLifecycle();

        var steps = LifecycleTimeline.stepsOf("managedBean");
        var phases = steps.stream().map(LifecycleTimeline.Step::phase).toList();

        assertThat(phases.indexOf("constructor")).isLessThan(phases.indexOf("dependencies"));
        assertThat(phases.indexOf("bpp-before")).isLessThan(phases.indexOf("postConstruct"));
        assertThat(phases.indexOf("postConstruct")).isLessThan(phases.indexOf("afterPropertiesSet"));
        assertThat(phases.indexOf("afterPropertiesSet")).isLessThan(phases.indexOf("bpp-after"));
        assertThat(phases.indexOf("preDestroy")).isLessThan(phases.indexOf("destroy"));
    }

    @Test
    @DisplayName("Нарушение порядка обнаруживается")
    void detectsViolations() {
        LifecycleLog.record("5a-postConstruct:broken");
        LifecycleLog.record("1-constructor:broken");

        assertThat(LifecycleTimeline.validate("broken"))
                .isNotEmpty()
                .anyMatch(v -> v.rule().equals("порядок шагов"));
    }

    @Test
    @DisplayName("Отсутствие бина в журнале — тоже сообщается явно")
    void reportsMissingBean() {
        assertThat(LifecycleTimeline.validate("нет-такого"))
                .singleElement()
                .satisfies(v -> assertThat(v.rule()).isEqualTo("нет данных"));
    }

    @Test
    @DisplayName("Singleton уничтожается, prototype — нет")
    void distinguishesDestroyedBeans() {
        runFullLifecycle();

        assertThat(LifecycleTimeline.wasDestroyed("managedBean")).isTrue();
        assertThat(LifecycleTimeline.wasDestroyed("prototypeWithDestroy"))
                .as("контейнер не управляет уничтожением prototype-бинов")
                .isFalse();
    }

    @Test
    @DisplayName("Сводка показывает, сколько шагов прошёл каждый бин")
    void summarisesSteps() {
        runFullLifecycle();

        assertThat(LifecycleTimeline.summary())
                .containsEntry("managedBean", 11)
                .containsEntry("prototypeWithDestroy", 1)
                .containsEntry("backgroundWorker", 2);
    }
}

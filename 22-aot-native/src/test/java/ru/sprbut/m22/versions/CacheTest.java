package ru.sprbut.m22.versions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Слайд «Версии»: jakarta.annotation в жизненном цикле бина")
final class CacheTest {

    @Test
    @DisplayName("@PostConstruct из jakarta вызывается контейнером Boot 3")
    void callsPostConstruct() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(Cache.class);
            context.refresh();
            assertThat(
                "jakarta PostConstruct cannot run after the bean is built",
                context.getBean(Cache.class).events(),
                contains("warm")
            );
        }
    }

    @Test
    @DisplayName("@PreDestroy срабатывает при закрытии контекста")
    void callsPreDestroy() {
        Cache cache;
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(Cache.class);
            context.refresh();
            cache = context.getBean(Cache.class);
        }
        assertThat(
            "jakarta PreDestroy cannot run when the context closes",
            cache.events(),
            contains("warm", "flush")
        );
    }

    @Test
    @DisplayName("вне контейнера аннотации не значат ничего — это метаданные, а не поведение")
    void dontRunLifecycleWithoutContainer() {
        assertThat(
            "annotations alone cannot trigger the lifecycle",
            new Cache().events().size(),
            equalTo(0)
        );
    }
}

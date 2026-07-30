package ru.sprbut.m21.scopes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@DisplayName("Слайд «Типичные ошибки»: prototype внутри singleton")
final class PlainScopeConfigTest {

    @Test
    @DisplayName("prototype, внедрённый в singleton, застывает на первом экземпляре")
    void dontRenewPrototypeInsideSingleton() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(PlainScopeConfig.class)) {
            Gate gate = context.getBean(Gate.class);
            assertThat(
                "prototype injected once cannot keep handing out the same number",
                gate.admit(),
                equalTo(gate.admit())
            );
        }
    }

    @Test
    @DisplayName("сам контейнер отдаёт новый prototype на каждый запрос")
    void renewsPrototypeOnEachLookup() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(PlainScopeConfig.class)) {
            assertThat(
                "direct lookup cannot produce a fresh prototype instance",
                context.getBean(Ticket.class).number(),
                not(equalTo(context.getBean(Ticket.class).number()))
            );
        }
    }

    @Test
    @DisplayName("proxyMode = TARGET_CLASS возвращает prototype его смысл")
    void renewsPrototypeThroughProxy() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ProxiedScopeConfig.class)) {
            Gate gate = context.getBean(Gate.class);
            assertThat(
                "scoped proxy cannot fetch a new ticket per call",
                gate.admit(),
                not(equalTo(gate.admit()))
            );
        }
    }
}

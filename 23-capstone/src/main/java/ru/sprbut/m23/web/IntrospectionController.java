package ru.sprbut.m23.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sprbut.m23.audit.AuditTrail;
import ru.sprbut.m23.extended.BeanCard;
import ru.sprbut.m23.extended.ContextMap;

/**
 * Приложение, объясняющее само себя, по HTTP.
 * <p>
 * {@code /api/introspection/beans} показывает, чем бины стали внутри контейнера,
 * а {@code /api/introspection/audit} — что успел записать аспект.
 * Вместе они отвечают на вопрос, ради которого затевался весь курс:
 * почему код в исходниках и поведение в рантайме — это не одно и то же.
 */
@RestController
@RequestMapping("/api/introspection")
public final class IntrospectionController {

    private final ContextMap map;

    private final AuditTrail trail;

    public IntrospectionController(ContextMap map, AuditTrail trail) {
        this.map = map;
        this.trail = trail;
    }

    @GetMapping("/beans")
    public List<BeanCard> beans() {
        return this.map.cards();
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return this.trail.records();
    }
}

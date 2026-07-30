package ru.sprbut.m17.stereotypes;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Слайды 140–144: {@code @Component}, {@code @Repository}, {@code @Controller},
 * {@code @Service}.
 * <p>
 * Технически все четыре — одно и то же: {@code @Repository}, {@code @Service}
 * и {@code @Controller} помечены мета-аннотацией {@code @Component}, и сканер
 * находит их одинаково (модуль 06).
 * <p>
 * Разница не в механике, а в двух других вещах:
 * <ul>
 *   <li><b>смысл</b> — по стереотипу видно роль класса в слоистой архитектуре;</li>
 *   <li><b>поведение</b> — на стереотип можно навесить обработку.
 *       {@code @Repository} включает трансляцию исключений драйвера БД
 *       в общую иерархию {@code DataAccessException}, а {@code @Controller}
 *       делает класс видимым для {@code DispatcherServlet}.</li>
 * </ul>
 */
public final class Stereotypes {

    private Stereotypes() {
    }

    @Component
    public static class PlainComponent {
        public String role() {
            return "component";
        }
    }

    @Service
    public static class BusinessService {
        public String role() {
            return "service";
        }
    }

    /**
     * {@code @Repository} — единственный стереотип, который <b>меняет поведение</b>
     * без дополнительной настройки: включается
     * {@code PersistenceExceptionTranslationPostProcessor}.
     */
    @Repository
    public static class DataRepository {
        public String role() {
            return "repository";
        }
    }

    @Controller
    public static class WebController {
        public String role() {
            return "controller";
        }
    }

    /** Класс без стереотипа — сканер его не найдёт. */
    public static class NotAComponent {
        public String role() {
            return "не бин";
        }
    }
}

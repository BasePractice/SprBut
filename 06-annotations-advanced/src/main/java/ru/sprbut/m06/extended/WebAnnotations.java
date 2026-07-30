package ru.sprbut.m06.extended;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мини-модель веб-аннотаций Spring — чтобы {@link MergedAnnotationScanner}
 * было на чём показать слияние значений.
 * <p>
 * Реальные {@code @GetMapping} и {@code @RestController} устроены ровно так же:
 * композиция мета-аннотаций плюс {@code @AliasFor} для переопределения элементов.
 */
public final class WebAnnotations {

    private WebAnnotations() {
    }

    public enum HttpMethod { ANY, GET, POST, DELETE }

    /** Базовая аннотация — аналог {@code @RequestMapping}. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface RequestMapping {
        String path() default "/";

        HttpMethod method() default HttpMethod.ANY;

        String[] produces() default {};
    }

    /**
     * Композиция: {@code @GetMapping("/users")} должен превратиться
     * в {@code @RequestMapping(path = "/users", method = GET)}.
     * <p>
     * {@code value} явно объявлен алиасом для {@code path} мета-аннотации —
     * без {@code @AliasFor} слияние по имени не сработало бы: имена разные.
     */
    @RequestMapping(method = HttpMethod.GET)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    // ANNOTATION_TYPE нужен, чтобы @GetMapping можно было навесить на другую
    // аннотацию — то есть построить композицию второго уровня (@GetJson)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    public @interface GetMapping {

        @AliasFor(annotation = RequestMapping.class, attribute = "path")
        String value() default "/";

        /** Одноимённый элемент переопределяет мета-аннотацию и без {@code @AliasFor}. */
        String[] produces() default {};
    }

    /** Второй уровень композиции: JSON-специализация {@code @GetMapping}. */
    @GetMapping(produces = "application/json")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface GetJson {

        @AliasFor(annotation = RequestMapping.class, attribute = "path")
        String value() default "/";
    }

    /** Алиас, указывающий на несуществующий элемент — источник ошибки конфигурации. */
    @RequestMapping
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BrokenMapping {

        @AliasFor(annotation = RequestMapping.class, attribute = "нетТакогоЭлемента")
        String value() default "/";
    }

    /** Подопытный контроллер. */
    @SuppressWarnings("unused")
    public static class UserController {

        @RequestMapping(path = "/raw", method = HttpMethod.POST, produces = "text/plain")
        public void raw() {
        }

        @GetMapping("/users")
        public void list() {
        }

        @GetMapping(value = "/users/active", produces = {"application/json", "application/xml"})
        public void listActive() {
        }

        @GetJson("/users/json")
        public void json() {
        }

        @BrokenMapping("/broken")
        public void broken() {
        }

        public void plain() {
        }
    }
}

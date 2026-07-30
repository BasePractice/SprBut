package ru.sprbut.m06;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Слайд 55: «{@code @RestController} = {@code @Controller} + {@code @ResponseBody}».
 * <p>
 * Это <b>композиция мета-аннотаций</b> — приём, на котором держится вся
 * декларативная часть Spring. Никакого «наследования аннотаций» в языке нет:
 * {@code @RestController} — просто аннотация, на которую навешены две другие.
 * <p>
 * Важное следствие: штатный {@code getAnnotation(Controller.class)} на классе
 * с {@code @RestController} вернёт {@code null} — язык мета-аннотации не
 * раскрывает. Их приходится искать рекурсивно вручную, что Spring и делает
 * в {@code AnnotatedElementUtils}.
 */
public final class Composition {

    private Composition() {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Controller {
        String value() default "";
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ResponseBody {
    }

    /** Композиция двух аннотаций в одну — ровно как в Spring. */
    @Controller
    @ResponseBody
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface RestController {
        String value() default "";
    }

    /** Композиция второго уровня — цепочки бывают и длиннее. */
    @RestController
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ApiController {
        String value() default "";
    }

    /** Наивная проверка: язык мета-аннотации не раскрывает. */
    public static boolean directlyAnnotated(Class<?> type, Class<? extends Annotation> annotation) {
        return type.isAnnotationPresent(annotation);
    }

    /**
     * Рекурсивный поиск аннотации, в том числе через мета-аннотации.
     * Это упрощённый {@code AnnotatedElementUtils.hasAnnotation} из Spring.
     */
    public static boolean metaAnnotated(Class<?> type, Class<? extends Annotation> annotation) {
        return findMeta(type.getAnnotations(), annotation, new HashSet<>());
    }

    private static boolean findMeta(Annotation[] annotations, Class<? extends Annotation> target,
                                    Set<Class<? extends Annotation>> visited) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.equals(target)) {
                return true;
            }
            if (isJavaBuiltin(type) || !visited.add(type)) {
                continue;
            }
            if (findMeta(type.getAnnotations(), target, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Полная цепочка мета-аннотаций от класса вглубь — то, что нужно печатать
     * при отладке «почему мой бин не подхватился».
     */
    public static List<String> annotationChain(Class<?> type) {
        List<String> chain = new ArrayList<>();
        collectChain(type.getAnnotations(), chain, new HashSet<>(), 0);
        return chain;
    }

    private static void collectChain(Annotation[] annotations, List<String> sink,
                                     Set<Class<? extends Annotation>> visited, int depth) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (isJavaBuiltin(type) || !visited.add(type)) {
                continue;
            }
            sink.add("  ".repeat(depth) + "@" + type.getSimpleName());
            collectChain(type.getAnnotations(), sink, visited, depth + 1);
        }
    }

    /**
     * Служебные мета-аннотации самого языка исключаем — иначе обход зациклится
     * на {@code @Retention}, помеченной {@code @Retention}, и утонет в шуме.
     */
    public static boolean isJavaBuiltin(Class<? extends Annotation> type) {
        return type.getName().startsWith("java.lang.annotation.")
                || type.getName().startsWith("jdk.internal.");
    }

    // --- Подопытные классы ---------------------------------------------------

    @RestController("users")
    public static class UserApi {
    }

    @ApiController("orders")
    public static class OrderApi {
    }

    @Controller
    public static class PlainController {
    }

    public static class NotAController {
    }
}

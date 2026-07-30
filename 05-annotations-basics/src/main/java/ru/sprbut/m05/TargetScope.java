package ru.sprbut.m05;

import ru.sprbut.m05.declarations.Level;
import ru.sprbut.m05.declarations.Marker;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 39: {@code @Target{TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}}.
 * <p>
 * {@code @Target} — это <b>ограничение компилятора</b>, а не рантайма. Он не даст
 * поставить аннотацию туда, где её быть не должно; в runtime проверять уже нечего.
 * <p>
 * Отдельная тонкость: <b>отсутствие</b> {@code @Target} означает «можно почти везде»,
 * а не «нигде».
 */
public final class TargetScope {

    private TargetScope() {
    }

    /** Куда разрешено ставить аннотацию — читается из её собственной мета-аннотации. */
    public static List<ElementType> allowedTargets(Class<? extends Annotation> annotation) {
        Target target = annotation.getAnnotation(Target.class);
        if (target == null) {
            return List.of();
        }
        return Arrays.asList(target.value());
    }

    public static boolean allowsFields(Class<? extends Annotation> annotation) {
        return allowedTargets(annotation).contains(ElementType.FIELD);
    }

    public static boolean allowsTypes(Class<? extends Annotation> annotation) {
        return allowedTargets(annotation).contains(ElementType.TYPE);
    }

    /**
     * Класс-пример: аннотации расставлены по всем местам, которые перечислены
     * на слайде. Он же служит подопытным для тестов.
     */
    @Marker
    @Level("класс")
    @SuppressWarnings("unused")
    public static class Annotated {

        @Level("поле")
        private String field;

        public Annotated() {
        }

        @Marker
        @Level("метод")
        public void method(String parameter) {
            // LOCAL_VARIABLE: аннотация на локальной переменной вообще не попадает
            // в class-файл — читать её в runtime невозможно в принципе
            @SuppressWarnings("unused")
            String local = parameter;
        }
    }
}

package ru.sprbut.m12.extended;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>Расширенный пример модуля 12.</b>
 * <p>
 * Аудитор точек внедрения: разбирает класс рефлексией и выносит вердикт —
 * насколько удачно в нём организованы зависимости. Каждое правило взято
 * прямо со слайдов 91–95, но здесь оно становится проверяемым:
 * <ul>
 *   <li><b>конструктор предпочтителен</b> (слайд 92) — проверяем, что поля
 *       {@code final} и зависимости обязательны;</li>
 *   <li><b>внедрение в поле мешает тестам</b> (слайд 93) — проверяем, можно ли
 *       собрать объект обычным {@code new} с готовыми зависимостями;</li>
 *   <li><b>Service Locator — антипаттерн</b> (слайд 95) — ловим
 *       {@code ApplicationContextAware} и обращения к контейнеру;</li>
 *   <li>слишком много зависимостей — сигнал, что класс делает слишком много.</li>
 * </ul>
 * Такую проверку не грех повесить в архитектурный тест проекта: она находит
 * ровно те классы, которые потом невозможно протестировать.
 */
public final class InjectionAudit {

    /** Порог, после которого количество зависимостей само по себе становится проблемой. */
    static final int TOO_MANY_DEPENDENCIES = 5;

    private InjectionAudit() {
    }

    public enum Style { CONSTRUCTOR, SETTER, FIELD, SERVICE_LOCATOR, NONE }

    /** Итог аудита одного класса. */
    public record Report(Class<?> type,
                         List<Style> styles,
                         List<String> dependencies,
                         boolean testableWithoutContainer,
                         boolean allFieldsFinal,
                         List<String> warnings) {

        public Report {
            styles = List.copyOf(styles);
            dependencies = List.copyOf(dependencies);
            warnings = List.copyOf(warnings);
        }

        public boolean clean() {
            return warnings.isEmpty();
        }

        public Style primaryStyle() {
            return styles.isEmpty() ? Style.NONE : styles.get(0);
        }
    }

    public static Report audit(Class<?> type) {
        List<Style> styles = new ArrayList<>();
        List<String> dependencies = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Constructor<?> injectable = injectableConstructor(type);
        if (injectable != null && injectable.getParameterCount() > 0) {
            styles.add(Style.CONSTRUCTOR);
            Arrays.stream(injectable.getParameterTypes())
                    .map(Class::getSimpleName)
                    .forEach(dependencies::add);
        }

        List<Field> injectedFields = injectedFields(type);
        if (!injectedFields.isEmpty()) {
            styles.add(Style.FIELD);
            injectedFields.stream().map(f -> f.getType().getSimpleName()).forEach(dependencies::add);
            warnings.add("внедрение в поле: класс нельзя собрать обычным new — "
                    + "поля " + injectedFields.stream().map(Field::getName).toList());
        }

        List<Method> injectedSetters = injectedSetters(type);
        if (!injectedSetters.isEmpty()) {
            styles.add(Style.SETTER);
            injectedSetters.stream()
                    .map(m -> m.getParameterTypes()[0].getSimpleName())
                    .forEach(dependencies::add);
        }

        if (ApplicationContextAware.class.isAssignableFrom(type)) {
            styles.add(Style.SERVICE_LOCATOR);
            warnings.add("Service Locator: класс сам ходит за зависимостями в контейнер — "
                    + "они не видны в API и не подменяются в тесте");
        }

        boolean allFinal = allDependencyFieldsAreFinal(type);
        if (styles.contains(Style.CONSTRUCTOR) && !allFinal && injectedFields.isEmpty()
                && injectedSetters.isEmpty()) {
            warnings.add("зависимости внедрены конструктором, но поля не final — "
                    + "их всё ещё можно переприсвоить");
        }
        if (dependencies.size() > TOO_MANY_DEPENDENCIES) {
            warnings.add("зависимостей " + dependencies.size() + " — вероятно, класс делает слишком много");
        }
        if (styles.size() > 1 && !styles.contains(Style.SERVICE_LOCATOR)) {
            warnings.add("смешаны способы внедрения: " + styles);
        }

        boolean testable = styles.contains(Style.CONSTRUCTOR)
                && injectedFields.isEmpty()
                && !ApplicationContextAware.class.isAssignableFrom(type);

        return new Report(type, styles, dependencies, testable, allFinal, warnings);
    }

    /**
     * Правило Spring: если конструктор один, он и используется — аннотация не нужна.
     * Если их несколько, нужен явный {@code @Autowired} или {@code @Inject}.
     */
    static Constructor<?> injectableConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        return Arrays.stream(constructors)
                .filter(c -> isAnnotated(c, Autowired.class) || isAnnotated(c, Inject.class))
                .findFirst()
                .orElse(null);
    }

    static List<Field> injectedFields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            Arrays.stream(c.getDeclaredFields())
                    .filter(f -> isAnnotated(f, Autowired.class)
                            || isAnnotated(f, Inject.class)
                            || isAnnotated(f, Resource.class))
                    .forEach(result::add);
        }
        return result;
    }

    static List<Method> injectedSetters(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> isAnnotated(m, Autowired.class)
                        || isAnnotated(m, Inject.class)
                        || isAnnotated(m, Resource.class))
                .sorted(java.util.Comparator.comparing(Method::getName))
                .toList();
    }

    /** Все ли нестатические поля объявлены {@code final}. */
    static boolean allDependencyFieldsAreFinal(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !f.isSynthetic() && !Modifier.isStatic(f.getModifiers()))
                .allMatch(f -> Modifier.isFinal(f.getModifiers()));
    }

    private static boolean isAnnotated(java.lang.reflect.AnnotatedElement element,
                                       Class<? extends Annotation> annotation) {
        return element.isAnnotationPresent(annotation);
    }
}

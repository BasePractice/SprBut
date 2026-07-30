package ru.sprbut.m06.extended;

import ru.sprbut.m06.Composition;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Расширенный пример модуля 06.</b>
 * <p>
 * Сканер «слитых» аннотаций — рабочая мини-версия
 * {@code AnnotatedElementUtils.findMergedAnnotation} из Spring. Он делает то,
 * чего <b>не делает сам язык</b>:
 * <ul>
 *   <li>находит аннотацию через произвольно длинную цепочку мета-аннотаций
 *       ({@code @ApiController} → {@code @RestController} → {@code @Controller});</li>
 *   <li>сливает значения элементов: то, что задано в композитной аннотации,
 *       переопределяет значение в мета-аннотации, а незаданное берётся из
 *       {@code default} (слайд 51);</li>
 *   <li>уважает {@link AliasFor} — явное указание, какой элемент какой переопределяет;</li>
 *   <li>поднимается по иерархии классов, потому что {@code @Inherited} работает
 *       только для аннотаций типов и только напрямую (слайд 53);</li>
 *   <li>раскрывает {@code @Repeatable}-контейнеры (слайд 47).</li>
 * </ul>
 * Это и есть ответ на вопрос «почему {@code @RestController} ведёт себя как
 * {@code @Controller}»: не потому, что так устроена Java, а потому, что так
 * написан читающий код.
 */
public final class MergedAnnotationScanner {

    private MergedAnnotationScanner() {
    }

    /** Найденная аннотация вместе со слитыми значениями элементов. */
    public record Merged(Class<? extends Annotation> type,
                         Map<String, Object> attributes,
                         List<String> metaPath) {

        public Merged {
            attributes = Map.copyOf(attributes);
            metaPath = List.copyOf(metaPath);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String attribute) {
            return (T) attributes.get(attribute);
        }

        public String getString(String attribute) {
            return String.valueOf(attributes.get(attribute));
        }
    }

    /**
     * Ищет аннотацию на элементе, раскрывая мета-аннотации и сливая значения.
     */
    public static <A extends Annotation> Optional<Merged> find(AnnotatedElement element, Class<A> target) {
        return search(element.getAnnotations(), target);
    }

    /**
     * То же самое, но с подъёмом по иерархии классов — за {@code @Inherited},
     * которое на такое не способно.
     */
    public static <A extends Annotation> Optional<Merged> findOnHierarchy(Class<?> type, Class<A> target) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            Optional<Merged> found = search(c.getDeclaredAnnotations(), target);
            if (found.isPresent()) {
                return found;
            }
        }
        // интерфейсы — язык на них не смотрит вообще, а Spring смотрит
        for (Class<?> iface : allInterfaces(type)) {
            Optional<Merged> found = search(iface.getDeclaredAnnotations(), target);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    /**
     * Обход в ширину по цепочке мета-аннотаций. Ширина, а не глубина, —
     * чтобы ближайшее к элементу объявление выигрывало.
     */
    private static <A extends Annotation> Optional<Merged> search(Annotation[] roots, Class<A> target) {
        Deque<Node> queue = new ArrayDeque<>();
        Set<Class<? extends Annotation>> visited = new HashSet<>();

        for (Annotation annotation : roots) {
            if (!Composition.isJavaBuiltin(annotation.annotationType())) {
                queue.add(new Node(annotation, List.of()));
            }
        }

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            Class<? extends Annotation> type = node.annotation().annotationType();
            if (!visited.add(type)) {
                continue;
            }

            List<Annotation> path = new ArrayList<>(node.path());
            path.add(node.annotation());

            if (type.equals(target)) {
                return Optional.of(merge(target, path));
            }

            for (Annotation meta : type.getAnnotations()) {
                if (!Composition.isJavaBuiltin(meta.annotationType())) {
                    queue.add(new Node(meta, path));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Слияние значений вдоль пути. Базой служат значения самой целевой
     * аннотации, поверх накладываются переопределения композитных — начиная
     * с самой дальней и заканчивая ближайшей к элементу, чтобы ближайшая
     * выиграла.
     */
    private static Merged merge(Class<? extends Annotation> target, List<Annotation> path) {
        Annotation targetAnnotation = path.get(path.size() - 1);
        Map<String, Object> attributes = new LinkedHashMap<>(rawAttributes(targetAnnotation));

        for (int i = path.size() - 2; i >= 0; i--) {
            applyOverrides(path.get(i), target, attributes);
        }

        List<String> metaPath = path.stream()
                .map(a -> "@" + a.annotationType().getSimpleName())
                .toList();
        return new Merged(target, attributes, metaPath);
    }

    /**
     * Переопределения, которые композитная аннотация вносит в целевую.
     * <p>
     * Два правила, оба как в Spring:
     * <ol>
     *   <li>элемент с {@link AliasFor}, указывающим на целевую аннотацию,
     *       переопределяет названный там элемент;</li>
     *   <li>элемент с совпадающим именем переопределяет одноимённый —
     *       но только если его значение отличается от {@code default}
     *       (иначе «незаданный» элемент затирал бы осмысленное значение).</li>
     * </ol>
     */
    private static void applyOverrides(Annotation source, Class<? extends Annotation> target,
                                       Map<String, Object> attributes) {
        for (Method element : source.annotationType().getDeclaredMethods()) {
            Object value = read(element, source);

            AliasFor alias = element.getAnnotation(AliasFor.class);
            if (alias != null && alias.annotation().equals(target)) {
                String name = alias.attribute().isBlank() ? element.getName() : alias.attribute();
                if (!attributes.containsKey(name)) {
                    throw new IllegalStateException("@AliasFor указывает на несуществующий элемент '"
                            + name + "' аннотации @" + target.getSimpleName());
                }
                attributes.put(name, value);
                continue;
            }

            if (attributes.containsKey(element.getName()) && !Objects.deepEquals(value, element.getDefaultValue())) {
                attributes.put(element.getName(), value);
            }
        }
    }

    /** Все элементы аннотации со значениями конкретного использования. */
    public static Map<String, Object> rawAttributes(Annotation annotation) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Method element : annotation.annotationType().getDeclaredMethods()) {
            result.put(element.getName(), read(element, annotation));
        }
        return result;
    }

    /**
     * Все аннотации элемента, включая раскрытые {@code @Repeatable}-контейнеры
     * и мета-аннотации. Полезно для отладки «что вообще навешано на класс».
     */
    public static List<String> flatten(AnnotatedElement element) {
        List<String> result = new ArrayList<>();
        Deque<Annotation> queue = new ArrayDeque<>(List.of(element.getAnnotations()));
        Set<Class<? extends Annotation>> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            Annotation annotation = queue.poll();
            Class<? extends Annotation> type = annotation.annotationType();
            if (Composition.isJavaBuiltin(type) || !visited.add(type)) {
                continue;
            }
            result.add("@" + type.getSimpleName());
            queue.addAll(List.of(type.getAnnotations()));
        }
        result.sort(String::compareTo);
        return result;
    }

    private static Object read(Method element, Annotation annotation) {
        try {
            element.setAccessible(true);
            return element.invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не прочитать элемент " + element.getName(), e);
        }
    }

    private static List<Class<?>> allInterfaces(Class<?> type) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Class<?> iface : c.getInterfaces()) {
                if (!result.contains(iface)) {
                    result.add(iface);
                    result.addAll(allInterfaces(iface));
                }
            }
        }
        return result;
    }

    private record Node(Annotation annotation, List<Annotation> path) {
    }
}

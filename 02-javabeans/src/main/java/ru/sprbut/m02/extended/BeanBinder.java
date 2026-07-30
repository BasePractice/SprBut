package ru.sprbut.m02.extended;

import ru.sprbut.m02.classic.BeanConventions;
import ru.sprbut.m02.classic.IntrospectionExample;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * <b>Расширенный пример модуля 02.</b>
 * <p>
 * Мини-биндер конфигурации: заполняет JavaBean из плоской карты «ключ → строка»,
 * ровно как {@code @ConfigurationProperties} заполняет объект из
 * {@code application.yaml}. Именно ради этого сценария соглашение JavaBeans
 * вообще существует — контейнеру нужен конструктор без параметров, чтобы создать
 * объект, и сеттеры, чтобы его наполнить.
 * <p>
 * Заодно показывает границу применимости соглашения: тот же биндер на
 * {@code record} не работает вовсе (нет ни конструктора без параметров, ни
 * сеттеров) — поэтому Spring Boot для immutable-конфигов пришлось учить
 * отдельному режиму constructor binding.
 */
public final class BeanBinder {

    /** Конвертеры строки в целевой тип свойства. */
    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.ofEntries(
            Map.entry(String.class, s -> s),
            Map.entry(int.class, Integer::parseInt),
            Map.entry(Integer.class, Integer::valueOf),
            Map.entry(long.class, Long::parseLong),
            Map.entry(Long.class, Long::valueOf),
            Map.entry(double.class, Double::parseDouble),
            Map.entry(Double.class, Double::valueOf),
            Map.entry(boolean.class, Boolean::parseBoolean),
            Map.entry(Boolean.class, Boolean::valueOf),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(LocalDate.class, LocalDate::parse),
            Map.entry(UUID.class, UUID::fromString)
    );

    private BeanBinder() {
    }

    /** Результат биндинга: заполненный объект и список того, что не удалось привязать. */
    public record BindResult<T>(T bean, List<String> boundProperties, List<String> ignoredKeys) {

        public BindResult {
            boundProperties = List.copyOf(boundProperties);
            ignoredKeys = List.copyOf(ignoredKeys);
        }
    }

    /**
     * Создаёт бин конструктором без параметров и заполняет его из карты.
     *
     * @param type   класс, обязан подчиняться соглашению JavaBeans
     * @param values значения, ключ — имя свойства (поддерживается kebab-case: {@code first-name})
     * @throws IllegalArgumentException если класс не является JavaBean
     */
    @SuppressWarnings("unchecked")
    public static <T> BindResult<T> bind(Class<T> type, Map<String, String> values) {
        BeanConventions.Verdict verdict = BeanConventions.validateSpringStyle(type);
        if (!verdict.valid()) {
            throw new IllegalArgumentException(type.getSimpleName()
                    + " не является JavaBean: " + String.join("; ", verdict.violations()));
        }

        T bean = (T) BeanConventions.instantiateEmpty(type);
        List<String> bound = new ArrayList<>();
        List<String> ignored = new ArrayList<>();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String property = normalize(entry.getKey());
            PropertyDescriptor pd = IntrospectionExample.descriptor(type, property).orElse(null);
            if (pd == null || pd.getWriteMethod() == null) {
                // Неизвестный ключ не роняет биндинг — так же ведёт себя Spring по умолчанию
                ignored.add(entry.getKey());
                continue;
            }
            Object converted = convert(entry.getValue(), pd.getPropertyType(), property);
            invoke(pd.getWriteMethod(), bean, converted);
            bound.add(property);
        }

        bound.sort(String::compareTo);
        return new BindResult<>(bean, bound, ignored);
    }

    /**
     * Копирование одноимённых свойств между двумя бинами — упрощённый аналог
     * {@code BeanUtils.copyProperties}. Так собирают DTO из сущностей вручную,
     * пока не подключат MapStruct (модуль 10).
     */
    public static List<String> copyProperties(Object source, Object target) {
        List<String> copied = new ArrayList<>();
        for (PropertyDescriptor targetPd : IntrospectionExample.descriptors(target.getClass())) {
            if (targetPd.getWriteMethod() == null || "class".equals(targetPd.getName())) {
                continue;
            }
            PropertyDescriptor sourcePd = IntrospectionExample
                    .descriptor(source.getClass(), targetPd.getName())
                    .orElse(null);
            if (sourcePd == null || sourcePd.getReadMethod() == null) {
                continue;
            }
            if (!isAssignable(targetPd.getPropertyType(), sourcePd.getPropertyType())) {
                continue;
            }
            invoke(targetPd.getWriteMethod(), target, invoke(sourcePd.getReadMethod(), source));
            copied.add(targetPd.getName());
        }
        copied.sort(String::compareTo);
        return copied;
    }

    /**
     * Обратная операция: бин → плоская карта. Пригодится, чтобы напечатать
     * эффективную конфигурацию приложения.
     */
    public static Map<String, String> describe(Object bean) {
        Map<String, String> result = new LinkedHashMap<>();
        IntrospectionExample.toMap(bean)
                .forEach((key, value) -> result.put(key, String.valueOf(value)));
        return result;
    }

    /** {@code first-name} и {@code first_name} → {@code firstName}. */
    static String normalize(String key) {
        if (key.indexOf('-') < 0 && key.indexOf('_') < 0) {
            return key;
        }
        String[] parts = key.split("[-_]");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }

    static Object convert(String raw, Class<?> targetType, String property) {
        if (targetType.isEnum()) {
            return Arrays.stream(targetType.getEnumConstants())
                    .filter(c -> ((Enum<?>) c).name().equalsIgnoreCase(raw))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Свойство '" + property + "': '" + raw + "' не входит в "
                                    + Arrays.toString(targetType.getEnumConstants())));
        }
        Function<String, Object> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            throw new IllegalArgumentException("Свойство '" + property + "': нет конвертера для типа "
                    + targetType.getSimpleName());
        }
        try {
            return converter.apply(raw);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Свойство '" + property + "': значение '" + raw
                    + "' не приводится к " + targetType.getSimpleName(), e);
        }
    }

    private static boolean isAssignable(Class<?> target, Class<?> source) {
        return target.equals(source) || target.isAssignableFrom(source);
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к " + method.getName(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof RuntimeException re ? re : new IllegalStateException(cause);
        }
    }
}

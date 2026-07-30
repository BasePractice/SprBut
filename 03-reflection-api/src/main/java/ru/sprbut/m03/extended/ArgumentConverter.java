package ru.sprbut.m03.extended;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/**
 * Превращает строку из команды в объект нужного параметру типа.
 * <p>
 * Тип берётся из {@code Parameter#getType()} — то есть решение принимается
 * по метаданным, а не по внешнему знанию. Ровно так Spring MVC конвертирует
 * параметры запроса в аргументы метода контроллера.
 */
final class ArgumentConverter {

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.ofEntries(
            Map.entry(String.class, s -> s),
            Map.entry(CharSequence.class, s -> s),
            Map.entry(int.class, Integer::parseInt),
            Map.entry(Integer.class, Integer::valueOf),
            Map.entry(long.class, Long::parseLong),
            Map.entry(Long.class, Long::valueOf),
            Map.entry(short.class, Short::parseShort),
            Map.entry(Short.class, Short::valueOf),
            Map.entry(byte.class, Byte::parseByte),
            Map.entry(Byte.class, Byte::valueOf),
            Map.entry(double.class, Double::parseDouble),
            Map.entry(Double.class, Double::valueOf),
            Map.entry(float.class, Float::parseFloat),
            Map.entry(Float.class, Float::valueOf),
            Map.entry(boolean.class, Boolean::parseBoolean),
            Map.entry(Boolean.class, Boolean::valueOf),
            Map.entry(char.class, s -> s.charAt(0)),
            Map.entry(Character.class, s -> s.charAt(0)),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(BigInteger.class, BigInteger::new),
            Map.entry(LocalDate.class, LocalDate::parse),
            Map.entry(Object.class, s -> s)
    );

    private ArgumentConverter() {
    }

    static Object convert(String raw, Class<?> targetType) {
        if ("null".equals(raw)) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("null нельзя передать в примитивный параметр "
                        + targetType.getSimpleName());
            }
            return null;
        }
        if (targetType.isEnum()) {
            return Arrays.stream(targetType.getEnumConstants())
                    .filter(c -> ((Enum<?>) c).name().equalsIgnoreCase(raw))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("'" + raw + "' не входит в "
                            + Arrays.toString(targetType.getEnumConstants())));
        }
        Function<String, Object> converter = CONVERTERS.get(targetType);
        if (converter == null) {
            throw new IllegalArgumentException("Нет конвертера для типа " + targetType.getSimpleName());
        }
        try {
            return converter.apply(raw);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Значение '" + raw + "' не приводится к "
                    + targetType.getSimpleName(), e);
        }
    }

    static boolean supports(Class<?> type) {
        return type.isEnum() || CONVERTERS.containsKey(type);
    }
}
